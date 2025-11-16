<x-productor-wizard-layout>
    <div class="bg-gray pt-24 pb-24">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">

            @if (session('message'))
                <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6" role="alert">
                    <p>{{ session('message') }}</p>
                </div>
            @endif
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
                            <div class="flex items-center text-green-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-green-600 rounded-full"><svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg></div>
                                <span class="ml-2 font-semibold">Datos Básicos</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-green-600"></div>
                            <div class="flex items-center text-green-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-green-600 rounded-full"><svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg></div>
                                <span class="ml-2 font-semibold">Ubicación</span>
                            </div>
                            <div class="flex-auto border-t-2 transition duration-500 ease-in-out border-indigo-600"></div>
                            <div class="flex items-center text-indigo-600">
                                <div class="flex items-center justify-center w-10 h-10 border-2 border-indigo-600 rounded-full">3</div>
                                <span class="ml-2 font-semibold">Detalles</span>
                            </div>
                        </div>
                    </div>

                    {{-- Formulario del Paso 3 --}}
                    <form method="POST" action="{{ route('productor.unidades-productivas.store') }}" id="final-form">
                        @csrf
                        <h3 class="text-lg font-semibold text-gray-800 border-b pb-2 mb-4">Paso 3: Detalles Adicionales</h3>
                        <div class="bg-blue-100 border-l-4 border-blue-500 text-blue-700 p-4 mb-6" role="alert">
                            <p class="font-bold">Este paso es opcional</p>
                            <p>Puede completar estos detalles ahora o más adelante desde el panel de su campo</p>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {{-- Agua para Humanos --}}
                            <div class="space-y-4 p-4 bg-gray-50 rounded-lg border">
                                <h4 class="font-medium text-gray-700">Agua para Consumo Humano</h4>
                                <div>
                                    <x-label for="agua_humano_fuente_id" value="Fuente de Agua" />
                                    <select name="agua_humano_fuente_id" id="agua_humano_fuente_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                        <option value="">Seleccionar fuente</option>
                                        @foreach($fuentes_agua as $fuente)
                                            <option value="{{ $fuente->id }}" {{ old('agua_humano_fuente_id', $formData['agua_humano_fuente_id'] ?? '') == $fuente->id ? 'selected' : '' }}>{{ $fuente->nombre }}</option>
                                        @endforeach
                                    </select>
                                    <x-input-error for="agua_humano_fuente_id" class="mt-2" />
                                </div>
                                <div class="flex items-center">
                                    <input type="checkbox" name="agua_humano_en_casa" id="agua_humano_en_casa" value="1" {{ old('agua_humano_en_casa', $formData['agua_humano_en_casa'] ?? false) ? 'checked' : '' }} class="h-4 w-4 text-indigo-600 border-gray-300 rounded">
                                    <x-label for="agua_humano_en_casa" class="ml-2" value="Agua en la casa" />
                                </div>
                                <div>
                                    <div class="flex items-center">
                                        <x-label for="agua_humano_distancia" value="Distancia (metros)" />
                                        <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                            <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-12 left-1/2 -translate-x-1/2 w-72 text-center pointer-events-none">
                                                Si la fuente de agua no está en la casa, especifique la distancia en metros que debe recorrer para obtenerla.
                                            </div>
                                        </div>
                                    </div>
                                    <x-input type="number" name="agua_humano_distancia" id="agua_humano_distancia" :value="old('agua_humano_distancia', $formData['agua_humano_distancia'] ?? '')" class="mt-1 block w-full" />
                                    <x-input-error for="agua_humano_distancia" class="mt-2" />
                                </div>
                            </div>

                            {{-- Agua para Animales --}}
                            <div class="space-y-4 p-4 bg-gray-50 rounded-lg border">
                                <h4 class="font-medium text-gray-700">Agua para Consumo Animal</h4>
                                <div>
                                    <x-label for="agua_animal_fuente_id" value="Fuente de Agua" />
                                    <select name="agua_animal_fuente_id" id="agua_animal_fuente_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                        <option value="">Seleccionar fuente</option>
                                        @foreach($fuentes_agua as $fuente)
                                            <option value="{{ $fuente->id }}" {{ old('agua_animal_fuente_id', $formData['agua_animal_fuente_id'] ?? '') == $fuente->id ? 'selected' : '' }}>{{ $fuente->nombre }}</option>
                                        @endforeach
                                    </select>
                                    <x-input-error for="agua_animal_fuente_id" class="mt-2" />
                                </div>
                                <div>
                                    <div class="flex items-center">
                                        <x-label for="agua_animal_distancia" value="Distancia (metros)" />
                                        <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                            <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-12 left-1/2 -translate-x-1/2 w-80 text-center pointer-events-none">
                                                Distancia en metros desde el lugar de pastoreo o corral hasta la fuente de agua para los animales.
                                            </div>
                                        </div>
                                    </div>
                                    <x-input type="number" name="agua_animal_distancia" id="agua_animal_distancia" :value="old('agua_animal_distancia', $formData['agua_animal_distancia'] ?? '')" class="mt-1 block w-full" />
                                    <x-input-error for="agua_animal_distancia" class="mt-2" />
                                </div>
                            </div>

                            {{-- Pasto y Suelo --}}
                            <div class="space-y-4 p-4 bg-gray-50 rounded-lg border">
                                <h4 class="font-medium text-gray-700">Pasto y Suelo</h4>
                                <div>
                                    <x-label for="tipo_pasto_predominante_id" value="Tipo de Pasto Predominante" />
                                    <select name="tipo_pasto_predominante_id" id="tipo_pasto_predominante_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                        <option value="">Seleccionar tipo</option>
                                        @foreach($tipos_pasto as $tipo)
                                            <option value="{{ $tipo->id }}" {{ old('tipo_pasto_predominante_id', $formData['tipo_pasto_predominante_id'] ?? '') == $tipo->id ? 'selected' : '' }}>{{ $tipo->nombre }}</option>
                                        @endforeach
                                    </select>
                                    <x-input-error for="tipo_pasto_predominante_id" class="mt-2" />
                                </div>
                                <div>
                                    <x-label for="tipo_suelo_predominante_id" value="Tipo de Suelo Predominante" />
                                    <select name="tipo_suelo_predominante_id" id="tipo_suelo_predominante_id" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm">
                                        <option value="">Seleccionar tipo</option>
                                        @foreach($tipos_suelo as $tipo)
                                            <option value="{{ $tipo->id }}" {{ old('tipo_suelo_predominante_id', $formData['tipo_suelo_predominante_id'] ?? '') == $tipo->id ? 'selected' : '' }}>{{ $tipo->nombre }}</option>
                                        @endforeach
                                    </select>
                                    <x-input-error for="tipo_suelo_predominante_id" class="mt-2" />
                                </div>
                                <div class="flex items-center">
                                    <input type="checkbox" name="forrajeras_predominante" id="forrajeras_predominante" value="1" {{ old('forrajeras_predominante', $formData['forrajeras_predominante'] ?? false) ? 'checked' : '' }} class="h-4 w-4 text-indigo-600 border-gray-300 rounded">
                                    <x-label for="forrajeras_predominante" class="ml-2" value="Forrajeras predominantes" />
                                    <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                        <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-14 left-1/2 -translate-x-1/2 w-96 text-center pointer-events-none">
                                            Marque esta casilla si en sus pasturas predominan especies implantadas o mejoradas para la alimentación del ganado.
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {{-- Observaciones --}}
                            <div class="md:col-span-2 space-y-4 p-4 bg-gray-50 rounded-lg border">
                                 <h4 class="font-medium text-gray-700">Observaciones</h4>
                                <div>
                                    <div class="flex items-center">
                                        <x-label for="observaciones" value="Observaciones Adicionales" />
                                        <div x-data="{ tooltip: false }" @mouseenter="tooltip = true" @mouseleave="tooltip = false" class="relative ml-2">
                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 text-gray-400 cursor-pointer"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
                                            <div x-show="tooltip" style="display: none;" class="absolute z-10 p-2 text-xs text-white bg-black rounded-md -top-10 left-1/2 -translate-x-1/2 w-96 text-center pointer-events-none">
                                                Cualquier otra información relevante sobre la infraestructura, manejo, o características del campo.
                                            </div>
                                        </div>
                                    </div>
                                    <textarea name="observaciones" id="observaciones" rows="4" class="mt-1 block w-full border-gray-300 rounded-md shadow-sm" placeholder="Información adicional...">{{ old('observaciones', $formData['observaciones'] ?? '') }}</textarea>
                                    <x-input-error for="observaciones" class="mt-2" />
                                </div>
                            </div>
                        </div>
                    </form>

                    <div class="absolute bottom-0 left-0 w-full px-6 py-4 z-20">
                        <div class="flex justify-between">
                            <a href="{{ route('productor.unidades-productivas.create.step2') }}" class="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded-lg shadow-md">Anterior</a>
                            <button type="submit" form="final-form" class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg shadow-md">Finalizar y Guardar</button>
                        </div>
                    </div>
                </div>


            </div>
        </div>
    </div>
</x-productor-wizard-layout>