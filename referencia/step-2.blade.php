<x-productor-wizard-layout>
    <div class="bg-gray pt-24 pb-24">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
            @if (session('error'))
                <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6" role="alert">
                    <p>{{ session('error') }}</p>
                </div>
            @endif
            @if (session('message'))
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6" role="alert">
                    <p>{{ session('message') }}</p>
                </div>
            @endif

            <div class="bg-white shadow-lg rounded-xl relative overflow-hidden">
                <div class="relative z-10 p-6 pb-32">
                    {{-- Step Indicator --}}
                    <div class="mb-8">
                        <div class="flex items-center">
                            <div class="flex items-center text-green-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-green-600 rounded-full">
                                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                                </div>
                                <span class="ml-2 font-semibold">Datos Básicos</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-indigo-600"></div>
                            <div class="flex items-center text-indigo-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-indigo-600 rounded-full">2</div>
                                <span class="ml-2 font-semibold">Ubicación</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-gray-400"></div>
                            <div class="flex items-center text-gray-500">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-gray-400 rounded-full">3</div>
                                <span class="ml-2 font-semibold">Detalles</span>
                            </div>
                        </div>
                    </div>

                    <h3 class="text-lg font-semibold text-gray-800 border-b pb-2 mb-4">Paso 2: Ubicación Geográfica</h3>

                    <div class="p-8 text-center bg-gray-50 rounded-lg border-2 border-dashed">
                        <div class="flex justify-center items-center mb-4">
                            <x-heroicon-o-map class="h-12 w-12 text-indigo-400" />
                        </div>
                        <h4 class="text-lg font-semibold text-gray-800 mb-2">Registro de Ubicación</h4>
                        <p class="text-gray-600 mb-6">
                            El siguiente paso es registrar la ubicación de su unidad productiva en el mapa.
                        </p>

                        @if(isset($formData['latitud']) && isset($formData['longitud']))
                            <div class="mb-6">
                                <p class="text-sm text-gray-500">Ubicación guardada:</p>
                                <div class="flex items-center justify-center text-lg font-bold text-green-600 bg-green-50 p-3 rounded-lg">
                                    <x-heroicon-s-check-circle class="h-6 w-6 mr-2" />
                                    <span>Latitud: {{ $formData['latitud'] }}, Longitud: {{ $formData['longitud'] }}</span>
                                </div>
                            </div>
                            <a href="{{ route('productor.up.ubicar') }}" class="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-lg shadow-md inline-flex items-center transition-all duration-200 ease-in-out transform hover:-translate-y-1 hover:shadow-xl">
                                <x-heroicon-o-pencil class="h-5 w-5 mr-2" />
                                Modificar Ubicación
                            </a>
                        @else
                            <a href="{{ route('productor.up.ubicar') }}" class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-3 px-6 rounded-lg shadow-md inline-flex items-center transition-all duration-200 ease-in-out transform hover:-translate-y-1 hover:shadow-xl">
                                <x-heroicon-o-map class="h-5 w-5 mr-2" />
                                Ir al Mapa
                            </a>
                        @endif
                    </div>

                    <div class="absolute bottom-0 left-0 w-full px-6 py-4 z-20">
                        <div class="flex justify-between">
                            <a href="{{ route('productor.unidades-productivas.create') }}" class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded-lg shadow-md">
                                Volver
                            </a>
                            <a href="{{ route('productor.unidades-productivas.create.step3') }}" class="bg-white hover:bg-gray-100 text-indigo-700 font-bold py-2 px-4 rounded-lg shadow-md">
                                Siguiente
                            </a>
                        </div>
                    </div>
                </div>


            </div>
        </div>
    </div>
</x-productor-wizard-layout>