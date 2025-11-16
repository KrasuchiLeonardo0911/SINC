<x-productor-wizard-layout>
    <div class="bg-gray pt-24 pb-24">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
            @if (session('error'))
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6" role="alert">
                    <p>{{ session('error') }}</p>
                </div>
            @endif

            <div class="bg-white shadow-lg rounded-xl relative overflow-hidden">
                <div class="relative z-10 p-6 pb-32">
                    {{-- Indicador de Pasos --}}
                    <div class="mb-8">
                        <div class="flex items-center">
                            <div class="flex items-center text-indigo-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-indigo-600 rounded-full">1</div>
                                <span class="ml-2 font-semibold">Datos Básicos</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-gray-400"></div>
                            <div class="flex items-center text-gray-500">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-gray-400 rounded-full">2</div>
                                <span class="ml-2 font-semibold">Ubicación</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-gray-400"></div>
                            <div class="flex items-center text-gray-500">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-gray-400 rounded-full">3</div>
                                <span class="ml-2 font-semibold">Detalles</span>
                            </div>
                        </div>
                    </div>

                    <form method="POST" action="{{ route('productor.unidades-productivas.store.step1') }}">
                        @csrf
                        <h3 class="text-lg font-semibold text-gray-800 border-b pb-2 mb-4">Paso 1: Información Básica</h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {{-- Nombre --}}
                            <div class="md:col-span-2">
                                <div class="flex items-center">
                                    <x-label for="nombre" value="Nombre del campo *" />
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-8 -left-24 w-64 text-center pointer-events-none">
                                            Es necesario asignar un nombre al campo para poder identificarla con mayor facilidad.
                                        </div>
                                    </div>
                                </div>
                                <x-input id="nombre" name="nombre" type="text" class="mt-1 block w-full" placeholder="Ej: Mi campo" :value="old('nombre', $formData['nombre'] ?? '')" required autofocus />
                                <x-input-error for="nombre" class="mt-2" />
                            </div>

                            {{-- Identificador Local --}}
                            <div>
                                <div class="flex items-center">
                                    <x-label for="identificador_local" value="N° de {{ $tipo_identificador_nombre ?: 'Identificador' }} *" />
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-10 -left-32 w-72 text-center pointer-events-none">
                                            Registro Nacional Sanitario de Productores Agropecuarios (RNSPA). Es obligatorio para identificarlo como productor.
                                        </div>
                                    </div>
                                </div>
                                <x-input id="identificador_local" name="identificador_local" type="text" class="mt-1 block w-full" placeholder="12.345.6.78901/23" :value="old('identificador_local', $formData['identificador_local'] ?? '')" required pattern="\d{2}\.\d{3}\.\d\.\d{5}\/\d{2}" title="El formato debe ser XX.XXX.X.XXXXX/XX" />
                                <p class="text-sm text-gray-500 mt-1">Solo necesitas escribir los números. El formato se agregará automáticamente.</p>
                                <x-input-error for="identificador_local" class="mt-2" />
                            </div>

                            {{-- Superficie --}}
                            <div>
                                <div class="flex items-center">
                                    <x-label for="superficie" value="Superficie (ha) *" />
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-8 -left-24 w-64 text-center pointer-events-none">
                                            Superficie en hectáreas del campo.
                                        </div>
                                    </div>
                                </div>
                                <x-input id="superficie" name="superficie" type="number" step="any" class="mt-1 block w-full" :value="old('superficie', $formData['superficie'] ?? '')" required />
                                <x-input-error for="superficie" class="mt-2" />
                            </div>

                            {{-- Municipio --}}
                            <div>
                                <x-label for="municipio_id" value="Municipio *" />
                                <select id="municipio_id" name="municipio_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                    <option value="">Seleccionar municipio</option>
                                    @foreach($municipios as $municipio)
                                        <option value="{{ $municipio->id }}" data-nombre="{{ $municipio->nombre }}" {{ old('municipio_id', $formData['municipio_id'] ?? '') == $municipio->id ? 'selected' : '' }}>{{ $municipio->nombre }}</option>
                                    @endforeach
                                </select>
                                <x-input-error for="municipio_id" class="mt-2" />
                            </div>

                            {{-- Paraje --}}
                            <div>
                                <x-label for="paraje_id" value="Paraje / Colonia (Opcional)" />
                                <select id="paraje_id" name="paraje_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm" disabled>
                                    <option value="">Primero elija un municipio</option>
                                </select>
                                <x-input-error for="paraje_id" class="mt-2" />
                                <div id="btn-crear-paraje-container" style="display: none;">
                                    <button type="button" id="btn-crear-paraje" class="text-indigo-600 hover:text-indigo-900 text-sm mt-2">
                                        Mi paraje no está en la lista
                                    </button>
                                </div>
                            </div>

                            {{-- Condicion Tenencia --}}
                            <div>
                                <div class="flex items-center">
                                    <x-label for="condicion_tenencia_id" value="Condición de Tenencia *" />
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-10 -left-24 w-72 text-center pointer-events-none">
                                            Forma en que el productor posee u ocupa la tierra (Propietario, Arrendatario, etc.).
                                        </div>
                                    </div>
                                </div>
                                <select id="condicion_tenencia_id" name="condicion_tenencia_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                    <option value="">Seleccionar condición</option>
                                    @foreach($condiciones_tenencia as $condicion)
                                        <option value="{{ $condicion->id }}" {{ old('condicion_tenencia_id', $formData['condicion_tenencia_id'] ?? '') == $condicion->id ? 'selected' : '' }}>{{ $condicion->nombre }}</option>
                                    @endforeach
                                </select>
                                <x-input-error for="condicion_tenencia_id" class="mt-2" />
                            </div>

                            {{-- Habita --}}
                            <div class="md:col-span-2">
                                <div class="flex items-center">
                                    <input id="habita" name="habita" type="checkbox" value="1" {{ old('habita', $formData['habita'] ?? false) ? 'checked' : '' }} class="h-4 w-4 text-indigo-600 border-gray-300 rounded">
                                    <label for="habita" class="ml-2 block text-sm text-gray-900 cursor-pointer">Habita en el lugar</label>
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-8 -left-24 w-64 text-center pointer-events-none">
                                            Marque esta casilla si vive en esa ubicacion.
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="absolute bottom-0 left-0 w-full px-6 py-4 z-20">
                            <div class="flex justify-end">
                                <button type="submit" class="bg-white hover:bg-gray-100 text-indigo-700 font-bold py-2 px-4 rounded-lg shadow-md">
                                    Siguiente
                                </button>
                            </div>
                        </div>
                    </form>
                </div>


            </div>
        </div>
    </div>

    @push('scripts')
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const municipioSelect = document.getElementById('municipio_id');
            const parajeSelect = document.getElementById('paraje_id');
            const btnCrearParajeContainer = document.getElementById('btn-crear-paraje-container');
            const btnCrearParaje = document.getElementById('btn-crear-paraje');

            async function updateParajes(municipioId, selectedParajeId = null) {
                if (municipioId) {
                    parajeSelect.disabled = true;
                    parajeSelect.innerHTML = '<option>Cargando parajes...</option>';
                    btnCrearParajeContainer.style.display = 'none';

                    try {
                        const response = await fetch(`/api/municipios/${municipioId}/parajes`, {
                            headers: {
                                'Accept': 'application/json',
                                'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').getAttribute('content')
                            }
                        });
                        if (!response.ok) throw new Error('Error al cargar parajes');
                        
                        const parajes = await response.json();
                        
                        parajeSelect.innerHTML = '<option value="">Seleccionar paraje</option>';
                        parajes.forEach(paraje => {
                            const option = new Option(paraje.nombre, paraje.id);
                            if (selectedParajeId && paraje.id == selectedParajeId) {
                                option.selected = true;
                            }
                            parajeSelect.appendChild(option);
                        });

                        parajeSelect.disabled = false;
                        btnCrearParajeContainer.style.display = 'block';

                    } catch (error) {
                        console.error('Error:', error);
                        parajeSelect.innerHTML = '<option value="">Error al cargar parajes</option>';
                    }
                } else {
                    parajeSelect.disabled = true;
                    parajeSelect.innerHTML = '<option value="">Primero elija un municipio</option>';
                    btnCrearParajeContainer.style.display = 'none';
                }
            }

            municipioSelect.addEventListener('change', (e) => {
                updateParajes(e.target.value);
            });

            btnCrearParaje.addEventListener('click', () => {
                const selectedOption = municipioSelect.options[municipioSelect.selectedIndex];
                const municipioId = municipioSelect.value;
                const municipioNombre = selectedOption.dataset.nombre;

                window.dispatchEvent(new CustomEvent('open-crear-paraje-modal', {
                    detail: { municipioId, municipioNombre }
                }));
            });

            window.addEventListener('paraje-temporal-agregado', event => {
                const nuevoParajeNombre = event.detail.nombre;
                const option = new Option(`${nuevoParajeNombre} (Nuevo)`, nuevoParajeNombre, true, true);
                parajeSelect.appendChild(option);
            });

            // Initial load
            const initialMunicipioId = '{{ old("municipio_id", $formData["municipio_id"] ?? '') }}';
            const initialParajeId = '{{ old("paraje_id", $formData["paraje_id"] ?? '') }}';
            if (initialMunicipioId) {
                updateParajes(initialMunicipioId, initialParajeId);
            }

            const rnspaInput = document.getElementById('identificador_local');
            if (rnspaInput) {
                rnspaInput.addEventListener('input', function (e) {
                    let value = e.target.value.replace(/\D/g, '');
                    let formattedValue = '';
                    if (value.length > 0) {
                        formattedValue = value.substring(0, 2);
                    }
                    if (value.length > 2) {
                        formattedValue += '.' + value.substring(2, 5);
                    }
                    if (value.length > 5) {
                        formattedValue += '.' + value.substring(5, 6);
                    }
                    if (value.length > 6) {
                        formattedValue += '.' + value.substring(6, 11);
                    }
                    if (value.length > 11) {
                        formattedValue += '/' + value.substring(11, 13);
                    }
                    e.target.value = formattedValue;
                });
            }
        });
    </script>
    @endpush
</x-productor-wizard-layout>