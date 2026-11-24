package cl.inforcore.backend.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regiones y comunas de Chile (16 regiones, 346 comunas), las mismas que usaba
 * el frontend en src/data/regionesComunas.js desde la EP1.
 *
 * En la EP3 pasan a la base de datos (tablas inforcore_regiones e
 * inforcore_comunas) para que el frontend ya no tenga listas "en duro":
 * las pide a la API. Esta clase es solo la fuente de la carga inicial.
 *
 * Se usa LinkedHashMap para conservar el orden de norte a sur, que es el orden
 * en que se muestran en los selectores.
 */
public final class RegionesChile {

    private RegionesChile() {
        // Clase de datos: no se instancia.
    }

    public static Map<String, List<String>> regionesConComunas() {
        Map<String, List<String>> regiones = new LinkedHashMap<>();
        regiones.put("Región de Arica y Parinacota", List.of(
                "Arica", "Camarones", "Putre", "General Lagos"));
        regiones.put("Región de Tarapacá", List.of(
                "Iquique", "Alto Hospicio", "Pozo Almonte", "Camiña",
                "Colchane", "Huara", "Pica"));
        regiones.put("Región de Antofagasta", List.of(
                "Antofagasta", "Mejillones", "Sierra Gorda", "Taltal",
                "Calama", "Ollagüe", "San Pedro de Atacama", "Tocopilla",
                "María Elena"));
        regiones.put("Región de Atacama", List.of(
                "Copiapó", "Caldera", "Tierra Amarilla", "Chañaral",
                "Diego de Almagro", "Vallenar", "Alto del Carmen", "Freirina",
                "Huasco"));
        regiones.put("Región de Coquimbo", List.of(
                "La Serena", "Coquimbo", "Andacollo", "La Higuera",
                "Paihuano", "Vicuña", "Illapel", "Canela",
                "Los Vilos", "Salamanca", "Ovalle", "Combarbalá",
                "Monte Patria", "Punitaqui", "Río Hurtado"));
        regiones.put("Región de Valparaíso", List.of(
                "Valparaíso", "Casablanca", "Concón", "Juan Fernández",
                "Puchuncaví", "Quintero", "Viña del Mar", "Isla de Pascua",
                "Los Andes", "Calle Larga", "Rinconada", "San Esteban",
                "La Ligua", "Cabildo", "Papudo", "Petorca",
                "Zapallar", "Quillota", "La Calera", "Hijuelas",
                "La Cruz", "Nogales", "San Antonio", "Algarrobo",
                "Cartagena", "El Quisco", "El Tabo", "Santo Domingo",
                "San Felipe", "Catemu", "Llaillay", "Panquehue",
                "Putaendo", "Santa María", "Quilpué", "Limache",
                "Olmué", "Villa Alemana"));
        regiones.put("Región Metropolitana de Santiago", List.of(
                "Santiago", "Cerrillos", "Cerro Navia", "Conchalí",
                "El Bosque", "Estación Central", "Huechuraba", "Independencia",
                "La Cisterna", "La Florida", "La Granja", "La Pintana",
                "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo",
                "Lo Prado", "Macul", "Maipú", "Ñuñoa",
                "Pedro Aguirre Cerda", "Peñalolén", "Providencia", "Pudahuel",
                "Quilicura", "Quinta Normal", "Recoleta", "Renca",
                "San Joaquín", "San Miguel", "San Ramón", "Vitacura",
                "Puente Alto", "Pirque", "San José de Maipo", "Colina",
                "Lampa", "Tiltil", "San Bernardo", "Buin",
                "Calera de Tango", "Paine", "Melipilla", "Alhué",
                "Curacaví", "María Pinto", "San Pedro", "Talagante",
                "El Monte", "Isla de Maipo", "Padre Hurtado", "Peñaflor"));
        regiones.put("Región del Libertador General Bernardo O'Higgins", List.of(
                "Rancagua", "Codegua", "Coinco", "Coltauco",
                "Doñihue", "Graneros", "Las Cabras", "Machalí",
                "Malloa", "Mostazal", "Olivar", "Peumo",
                "Pichidegua", "Quinta de Tilcoco", "Rengo", "Requínoa",
                "San Vicente", "Pichilemu", "La Estrella", "Litueche",
                "Marchihue", "Navidad", "Paredones", "San Fernando",
                "Chépica", "Chimbarongo", "Lolol", "Nancagua",
                "Palmilla", "Peralillo", "Placilla", "Pumanque",
                "Santa Cruz"));
        regiones.put("Región del Maule", List.of(
                "Talca", "Constitución", "Curepto", "Empedrado",
                "Maule", "Pelarco", "Pencahue", "Río Claro",
                "San Clemente", "San Rafael", "Cauquenes", "Chanco",
                "Pelluhue", "Curicó", "Hualañé", "Licantén",
                "Molina", "Rauco", "Romeral", "Sagrada Familia",
                "Teno", "Vichuquén", "Linares", "Colbún",
                "Longaví", "Parral", "Retiro", "San Javier",
                "Villa Alegre", "Yerbas Buenas"));
        regiones.put("Región de Ñuble", List.of(
                "Chillán", "Bulnes", "Chillán Viejo", "El Carmen",
                "Pemuco", "Pinto", "Quillón", "San Ignacio",
                "Yungay", "Quirihue", "Cobquecura", "Coelemu",
                "Ninhue", "Portezuelo", "Ránquil", "Treguaco",
                "San Carlos", "Coihueco", "Ñiquén", "San Fabián",
                "San Nicolás"));
        regiones.put("Región del Biobío", List.of(
                "Concepción", "Coronel", "Chiguayante", "Florida",
                "Hualqui", "Lota", "Penco", "San Pedro de la Paz",
                "Santa Juana", "Talcahuano", "Tomé", "Hualpén",
                "Lebu", "Arauco", "Cañete", "Contulmo",
                "Curanilahue", "Los Álamos", "Tirúa", "Los Ángeles",
                "Antuco", "Cabrero", "Laja", "Mulchén",
                "Nacimiento", "Negrete", "Quilaco", "Quilleco",
                "San Rosendo", "Santa Bárbara", "Tucapel", "Yumbel",
                "Alto Biobío"));
        regiones.put("Región de La Araucanía", List.of(
                "Temuco", "Carahue", "Cunco", "Curarrehue",
                "Freire", "Galvarino", "Gorbea", "Lautaro",
                "Loncoche", "Melipeuco", "Nueva Imperial", "Padre Las Casas",
                "Perquenco", "Pitrufquén", "Pucón", "Saavedra",
                "Teodoro Schmidt", "Toltén", "Vilcún", "Villarrica",
                "Cholchol", "Angol", "Collipulli", "Curacautín",
                "Ercilla", "Lonquimay", "Los Sauces", "Lumaco",
                "Purén", "Renaico", "Traiguén", "Victoria"));
        regiones.put("Región de Los Ríos", List.of(
                "Valdivia", "Corral", "Lanco", "Los Lagos",
                "Máfil", "Mariquina", "Paillaco", "Panguipulli",
                "La Unión", "Futrono", "Lago Ranco", "Río Bueno"));
        regiones.put("Región de Los Lagos", List.of(
                "Puerto Montt", "Calbuco", "Cochamó", "Fresia",
                "Frutillar", "Los Muermos", "Llanquihue", "Maullín",
                "Puerto Varas", "Castro", "Ancud", "Chonchi",
                "Curaco de Vélez", "Dalcahue", "Puqueldón", "Queilén",
                "Quellón", "Quemchi", "Quinchao", "Osorno",
                "Puerto Octay", "Purranque", "Puyehue", "Río Negro",
                "San Juan de la Costa", "San Pablo", "Chaitén", "Futaleufú",
                "Hualaihué", "Palena"));
        regiones.put("Región de Aysén del General Carlos Ibáñez del Campo", List.of(
                "Coyhaique", "Lago Verde", "Aysén", "Cisnes",
                "Guaitecas", "Cochrane", "O'Higgins", "Tortel",
                "Chile Chico", "Río Ibáñez"));
        regiones.put("Región de Magallanes y de la Antártica Chilena", List.of(
                "Punta Arenas", "Laguna Blanca", "Río Verde", "San Gregorio",
                "Cabo de Hornos", "Antártica", "Porvenir", "Primavera",
                "Timaukel", "Natales", "Torres del Paine"));
        return regiones;
    }
}
