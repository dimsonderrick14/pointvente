package magasin

import com.csvreader.CsvReader
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON
import java.nio.charset.Charset

class PointVenteController {

    enum EtatReponseGeocoder {

        ERROR("ERROR", "Il y a eu une problème pour contacter les serveurs de Google"),

        INVALID_REQUEST("INVALID_REQUEST", "Cette requête de géocodage GeocoderRequest était invalide"),

        OK("OK", "La réponse contient une réponse GeocoderResponse valide."),

        OVER_QUERY_LIMIT("OVER_QUERY_LIMIT", "La page Web a dépassé le nombre de requêtes autorisées dans un laps de temps très court"),

        REQUEST_DENIED("REQUEST_DENIED", "La page web n'est pas autorisée à utiliser le géocodeur"),

        UNKNOWN_ERROR("UNKNOWN_ERROR", " Une requête de géocodage n'a pas pu être traitée en raison d'une erreur au niveau du serveur.\n" +
                "\n" +
                "La requête peut néanmoins aboutir si vous essayez à nouveau de l'envoyer"),

        ZERO_RESULTS("ZERO_RESULTS", "Le géocodage a réussi, mais aucun résultat n'a pu être trouvé pour cette demande de géocodage GeocoderRequest.\n" +
                "\n" +
                "Cela peut se produire lorsque l'on transmet au géocodeur une adresse inexistante ou un LatLng pour un emplacement trop éloigné")


        private String code

        private String message

        private EtatReponseGeocoder(String code, String message) {
            this.code = code
            this.message = message
        }

        public getCode() {
            return code;
        }

        public getMesssage() {
            return message;
        }
    }

    PointVenteService pointVenteService;

    private StringBuffer debugMessage
    private StringBuffer overquerylimit

    final static String FICHIER_POINT_VENTE = "resources/Testtechnique.csv"
    final static String GEOCODE_URL = 'http://maps.googleapis.com/maps/api/geocode/json'


    def sauverPointVente = {

        debugMessage = new StringBuffer()
        overquerylimit = new StringBuffer()

        try {

            def appHolder = ApplicationHolder.application.parentContext.getResource("classpath:$FICHIER_POINT_VENTE")
            Charset charset = Charset.forName("ISO-8859-1");
            CsvReader csvReferenceList = new CsvReader(appHolder.inputStream, charset);
            def http = new HTTPBuilder(GEOCODE_URL)
            PointVente marker
            int tentativeConnection = 0
            while (csvReferenceList.readRecord() && tentativeConnection < 10) {
                String ligne = csvReferenceList.getRawRecord()
                String[] splitedLigne = ligne.split(";")
                String addresse = splitedLigne[1];
                http.request(GET, JSON) {
                    uri.query = [sensor: 'false', address: addresse]
                    response.success = { resp, json ->
                        if (json.status == EtatReponseGeocoder.OK.getCode()) {
                            if (tentativeConnection > 0)
                                tentativeConnection--
                            marker = new PointVente()
                            marker.setName(splitedLigne[0])
                            marker.setAdresse(splitedLigne[1])
                            marker.setLat(json.results.geometry.location.lat.get(0))
                            marker.setLng(json.results.geometry.location.lng.get(0))
                            pointVenteService.sauverPointVente(marker)
                        } else if (json.status == EtatReponseGeocoder.INVALID_REQUEST.getCode() || json.status == EtatReponseGeocoder.REQUEST_DENIED.getCode()
                                || json.status == EtatReponseGeocoder.ZERO_RESULTS.getCode()) {
                            debugMessage.append(addresse).append('<br />');
                        } else if (json.status == EtatReponseGeocoder.OVER_QUERY_LIMIT.getCode()) {
                            tentativeConnection++
                            overquerylimit.append(addresse).append('<br />');
                            Thread.sleep(4000);
                        } else {
                            throw new Exception("Erreur inconnue")
                        }

                    }
                }

            }
            String debugMess = '<h1>Erreur lors import de certains points de vente : <br/> </h1>'
            if (debugMessage.length() > 0)
                debugMess = +debugMessage.toString()
            if (overquerylimit.length() > 0)
                debugMess += '<br/>' + '<b>' + EtatReponseGeocoder.OVER_QUERY_LIMIT.getMesssage() + ' pour les addresses</b> : <br />' + overquerylimit.toString()
            [debugMess: debugMess, tentativeConnection: tentativeConnection]
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
