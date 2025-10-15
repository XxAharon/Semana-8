1. Arquitectura y Flujo de la App
Pantalla Única (MainActivity): Toda la funcionalidad reside en una sola Activity. Esta Activity es la responsable de mostrar la interfaz de usuario, solicitar permisos y manejar la lógica del mapa.

Carga del Mapa: El mapa se carga estáticamente usando un <fragment> en el archivo activity_main.xml. Esto significa que el SupportMapFragment (el componente que muestra el mapa) es parte del diseño inicial de la MainActivity.

Inicialización (onCreate):

Al iniciar la MainActivity, se configura la vista (activity_main.xml).

Se obtiene una referencia al SupportMapFragment y se llama a getMapAsync(this). Esto inicia la carga del mapa en segundo plano.

La MainActivity implementa OnMapReadyCallback, por lo que el sistema llamará al método onMapReady() cuando el mapa esté listo.

Simultáneamente, se inicia la verificación de permisos de ubicación (VerificarPermisos()).

Manejo de Permisos: La app utiliza el ActivityResultLauncher moderno para solicitar permisos. Gestiona los casos en que el usuario los concede, los deniega una vez o los deniega repetidamente (lo que sugiere al usuario ir a la configuración del sistema).

Obtención de Ubicación (onMapReady y CargarUbicacion):

Cuando el mapa está cargado (onMapReady), la variable mapaCargado se establece en true. Si ya se tienen los permisos, se llama a CargarUbicacion().

CargarUbicacion() utiliza el FusedLocationProviderClient para obtener la última ubicación conocida del dispositivo.

Activa la capa "My Location" (setMyLocationEnabled(true)), que es la responsable de dibujar el punto azul en el mapa.

Adicionalmente, mueve la cámara del mapa a la ubicación encontrada y coloca un marcador manual (pin rojo).

2. Componentes Clave
MainActivity.java: El cerebro de la aplicación. Contiene toda la lógica para permisos, ubicación y la interacción con el mapa.

activity_main.xml: Define la interfaz visual. Contiene el SupportMapFragment para el mapa y un Button para buscar la ubicación.

AndroidManifest.xml: Archivo de configuración crucial. Aquí se declaran los permisos necesarios (Internet, ubicación fina y gruesa) y, lo más importante, es donde se debe colocar la Clave API de Google Maps.

build.gradle (Module: app): Define las dependencias del proyecto, como la librería de servicios de ubicación de Google (play-services-location) y la de mapas (play-services-maps).
