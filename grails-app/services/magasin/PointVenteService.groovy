package magasin

class PointVenteService {

    static transactional = true

    void sauverPointVente(PointVente marker) {

        if (!marker.save(flush: true)) {
            throw new Exception(message: "Invalide ou point vente vide")
        }

    }
}
