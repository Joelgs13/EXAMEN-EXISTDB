package joel.adat;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.modules.XQueryService;

/**
 * Programa que muestra las facturas de cada cliente desde eXist-DB.
 */
public class FacturasClientes {
    private static final String URI = "xmldb:exist://localhost:8080/exist/xmlrpc/db/ColeccionVentas";
    private static final String USER = "admin";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            // 1. Registrar la base de datos
            Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
            Database database = (Database) cl.getDeclaredConstructor().newInstance();
            DatabaseManager.registerDatabase(database);

            System.out.println("Conexión exitosa a la base de datos: " + URI);

            // 2. Conectar a la colección raíz
            Collection root = DatabaseManager.getCollection(URI, USER, PASSWORD);
            if (root == null) {
                System.err.println("No se pudo conectar con la colección raíz. Verifica el URI, usuario y contraseña.");
                return;
            }

            // 3. Crear la consulta XQuery
            String xquery = generarConsultaXQuery();

            // 4. Ejecutar la consulta XQuery
            ejecutarConsulta(root, xquery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Genera la consulta XQuery para obtener las facturas de cada cliente.
     *
     * @return la consulta XQuery como cadena.
     */
    private static String generarConsultaXQuery() {
        return """
        let $clientes := doc("clientes.xml")/clientes/clien
        let $facturas := doc("facturas.xml")/facturas/factura
        for $factura in $facturas
        let $cliente := $clientes[@numero = $factura/numcliente][1]
        return 
            <facturasclientes>
                <nombre>{data($cliente/nombre)}</nombre>
                <nufac>{data($factura/@numero)}</nufac>
            </facturasclientes>
        """;
    }


    /**
     * Ejecuta una consulta XQuery y muestra el resultado en pantalla.
     *
     * @param col    la colección donde se ejecutará la consulta.
     * @param xquery la consulta XQuery a ejecutar.
     * @throws Exception si ocurre algún error durante la ejecución.
     */
    private static void ejecutarConsulta(Collection col, String xquery) throws Exception {
        XQueryService xQueryService = (XQueryService) col.getService("XQueryService", "1.0");
        ResourceSet result = xQueryService.query(xquery);

        ResourceIterator iter = result.getIterator();
        if (!iter.hasMoreResources()) {
            System.out.println("No se encontraron resultados.");
            return;
        }

        System.out.println("Resultados de la consulta:");
        while (iter.hasMoreResources()) {
            System.out.println(iter.nextResource().getContent());
        }
    }
}
