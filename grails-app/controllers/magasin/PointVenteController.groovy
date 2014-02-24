package magasin

import com.csvreader.CsvReader
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON
import java.nio.charset.Charset

class PointVenteController {

    PointVenteService pointVenteService;

    final static String FICHIER_POINT_VENTE = "resources/Testtechnique.csv"
    final static String GEOCODE_URL = 'http://maps.googleapis.com/maps/api/geocode/json'


    def succes = {

    }


    def sauverPointVente = {

        try {
            def appHolder = ApplicationHolder.application.parentContext.getResource("classpath:$FICHIER_POINT_VENTE")

            Charset charset = Charset.forName("ISO-8859-1");
            CsvReader csvReferenceList = new CsvReader(appHolder.inputStream, charset);

            def http = new HTTPBuilder(GEOCODE_URL)

            PointVente marker


            while (csvReferenceList.readRecord()) {

                String ligne = csvReferenceList.getRawRecord()
                String[] splitedLigne = ligne.split(";")

                http.request(GET, JSON) {
                    uri.query = [sensor: 'true', address: splitedLigne[1]]

                    response.success = { resp, json ->
                        marker = new PointVente()
                        marker.setName(splitedLigne[0])
                        marker.setAdresse(splitedLigne[1])
                        marker.setLat(json.results.geometry.location.lat.get(0))
                        marker.setLng(json.results.geometry.location.lng.get(0))
                        pointVenteService.sauverPointVente(marker)

                    }
                }

            }
            redirect(action: 'succes')
        }
        catch (Exception e) {
            e.printStackTrace()
            render '<h1>Erreur lors import des points de vente <br/> </h1>' + e.getMessage()
        }
    }

    def recherchePointVente = {

        String latparam = params.latparam
        String lngparam = params.lngparam
        String rayon = params.rayon

        def resultat = pointVenteService.recherchePointVente(latparam, lngparam, rayon)

        render resultat as grails.converters.JSON
    }

    def visualiserPointVente = {

        String latparam = params.latparam
        String lngparam = params.lngparam
        String rayon = params.rayon

        [latparam: latparam, lngparam: lngparam, rayon: rayon]
    }


}
