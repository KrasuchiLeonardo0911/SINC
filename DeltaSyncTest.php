<?php

namespace Tests\Feature\Api\Movil;

use App\Models\User;
use App\Models\Productor;
use App\Models\UnidadProductiva;
use App\Models\Especie;
use App\Models\Raza;
use App\Models\CategoriaAnimal;
use App\Models\DeclaracionStock;
use App\Models\StockAnimal;
use App\Models\TipoRegistro;
use App\Models\MotivoMovimiento;
use Carbon\Carbon;
use Database\Seeders\RolesAndPermissionsSeeder;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class DeltaSyncTest extends TestCase
{
    use RefreshDatabase;

    protected User $user;
    protected Productor $productor;
    protected UnidadProductiva $unidadProductiva;
    protected DeclaracionStock $declaracion;
    protected $commonData;

    protected function setUp(): void
    {
        parent::setUp();
        $this->seed(RolesAndPermissionsSeeder::class);

        // Setup Usuario y Productor
        $this->user = User::factory()->create();
        $this->user->assignRole('productor');
        $this->productor = Productor::factory()->create(['usuario_id' => $this->user->id]);
        $this->unidadProductiva = UnidadProductiva::factory()->create();
        $this->productor->unidadesProductivas()->attach($this->unidadProductiva->id);

        // Datos comunes para StockAnimal
        $this->commonData = [
            'especie_id' => Especie::factory()->create()->id,
            'raza_id' => Raza::factory()->create()->id,
            'categoria_id' => CategoriaAnimal::factory()->create()->id,
            'tipo_registro_id' => TipoRegistro::factory()->create()->id,
            'motivo_movimiento_id' => MotivoMovimiento::factory()->create()->id,
            'unidad_productiva_id' => $this->unidadProductiva->id,
            'cantidad' => 10,
        ];

        // Declaración base
        $this->declaracion = DeclaracionStock::factory()->create([
            'productor_id' => $this->productor->id,
            'unidad_productiva_id' => $this->unidadProductiva->id,
        ]);
    }

    /** @test */
    public function test_full_sync_returns_all_records()
    {
        // Crear 2 movimientos
        StockAnimal::factory()->create(array_merge($this->commonData, [
            'declaracion_id' => $this->declaracion->id,
            'cantidad' => 5
        ]));
        StockAnimal::factory()->create(array_merge($this->commonData, [
            'declaracion_id' => $this->declaracion->id,
            'cantidad' => 8
        ]));

        $response = $this->actingAs($this->user, 'sanctum')
            ->getJson('/api/movil/cuaderno/movimientos');

        $response->assertStatus(200)
            ->assertJsonCount(2);
    }

    /** @test */
    public function test_delta_sync_returns_only_new_records()
    {
        // 1. Crear registro antiguo
        $oldDate = Carbon::now()->subDays(2);
        $oldRecord = StockAnimal::factory()->create(array_merge($this->commonData, [
            'declaracion_id' => $this->declaracion->id,
            'updated_at' => $oldDate,
            'created_at' => $oldDate,
        ]));

        // 2. Definir fecha de corte (Sincronización ocurrió ayer)
        $syncDate = Carbon::now()->subDay();

        // 3. Crear registro nuevo (Hoy)
        $newRecord = StockAnimal::factory()->create(array_merge($this->commonData, [
            'declaracion_id' => $this->declaracion->id,
            'updated_at' => Carbon::now(),
            'created_at' => Carbon::now(),
        ]));

        // 4. Petición con updated_after
        // Usar toDateTimeString para formato SQL compatible con SQLite/MySQL
        $response = $this->actingAs($this->user, 'sanctum')
            ->getJson('/api/movil/cuaderno/movimientos?updated_after=' . $syncDate->toDateTimeString());

        $response->assertStatus(200)
            ->assertJsonCount(1)
            ->assertJsonFragment(['id' => $newRecord->id])
            ->assertJsonMissing(['id' => $oldRecord->id]);
    }

    /** @test */
    public function test_delta_sync_includes_soft_deleted_records()
    {
        // 1. Crear un registro
        $record = StockAnimal::factory()->create(array_merge($this->commonData, [
            'declaracion_id' => $this->declaracion->id,
        ]));

        // 2. Definir fecha de sincronización (antes de borrar)
        $syncDate = Carbon::now()->subSecond();

        // 3. Borrar el registro (Soft Delete)
        $record->delete();

        // Asegurarse de que el updated_at cambió al borrar (Laravel lo hace autom., pero verificamos)
        $this->assertSoftDeleted('stock_animals', ['id' => $record->id]);

        // 4. Petición con updated_after
        $response = $this->actingAs($this->user, 'sanctum')
            ->getJson('/api/movil/cuaderno/movimientos?updated_after=' . $syncDate->toDateTimeString());

        if ($response->status() !== 200) {
            dump($response->content());
        }

        // Debe devolver el registro borrado para que la app lo borre localmente
        $response->assertStatus(200)
            ->assertJsonFragment(['id' => $record->id]);
            
        $data = $response->json();
        $deletedRecord = collect($data)->firstWhere('id', $record->id);
        
        $this->assertNotNull($deletedRecord['deleted_at'], 'El campo deleted_at debe estar presente y no ser nulo');
    }

    /** @test */
    public function test_init_endpoint_returns_sync_status_and_catalogs_version()
    {
        $response = $this->actingAs($this->user, 'sanctum')
            ->getJson('/api/movil/init');

        if ($response->status() !== 200) {
            dump($response->content());
        }

        $response->assertStatus(200)
            ->assertJsonStructure([
                'configuration' => [
                    'catalogs_version'
                ],
                'sync_status' => [
                    'stock_last_update',
                    'movements_last_update',
                    'sales_last_update'
                ]
            ]);
    }
}
