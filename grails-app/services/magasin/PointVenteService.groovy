package magasin

class PointVenteService {

    static transactional = true

    void sauverPointVente(PointVente marker) {

        if (!marker.save(flush: true)) {
            throw new Exception(message: "Invalide ou point vente vide")
        }

    }

    List recherchePointVente(def latparam, def lngparam, def rayon) {

        /* la formule Haversine pour aller requêter sur la table "PointVente" et calculer les points de vente dans un rayon de x km de l'internaute */

        String hql = "SELECT adresse,name,lat,lng, ( 3959 * acos( cos( radians(:latparam) ) * cos( radians( lat ) ) * cos( radians( lng )" +
                " - radians(:lngparam) ) + sin( radians(:latparam) ) * sin( radians( lat ) ) ) ) AS distance FROM PointVente " +
                "where ( 3959 * acos( cos( radians(:latparam) ) * cos( radians( lat ) ) * cos( radians( lng ) " +
                "- radians(:lngparam) ) + sin( radians(:latparam) ) * sin( radians( lat ) ) ) ) < :rayon  ";

        List<PointVente> pointVenteList = PointVente.executeQuery(hql, ['latparam': new Double(latparam),
                'lngparam': new Double(lngparam), 'rayon': new Double(rayon)])

        List listePointVenteMarker = []

        final int POS_ADRESSE = 0
        final int POS_NAME = 1
        final int POS_LAT = 2
        final int POS_LNG = 3
        final int POS_DISTANCE = 4

        for (Object[] fields : pointVenteList) {
            listePointVenteMarker << [
                    address: fields[POS_ADRESSE],
                    name: fields[POS_NAME],
                    lat: fields[POS_LAT],
                    lng: fields[POS_LNG],
                    distance: fields[POS_DISTANCE]
            ]
        }

        return listePointVenteMarker;

    }
}
