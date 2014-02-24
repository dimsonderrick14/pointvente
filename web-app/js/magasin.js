var map;
var markers = [
];
var infoWindow;
var RAYON = 20;

function initialize(rayon, latparam, lngparam) {
    var myLatlng = null;
    if (rayon == 'null') {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(localise_moi);
        } else {
            alert('Dommage, nous ne pourrons vous géolocaliser');
        }
    }
    else {
        myLatlng = new google.maps.LatLng(latparam, lngparam);
    }
    if (myLatlng != null) {
        recherchePointVente(rayon, myLatlng)
    }
}
function localise_moi(position) {
    var myLatlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
    recherchePointVente(RAYON, myLatlng);
}
function recherchePointVente(rayon, myLatlng) {
    map = new google.maps.Map(document.getElementById('map'), {
        center: myLatlng,
        zoom: 17,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });
    infoWindow = new google.maps.InfoWindow();
    rechercherPointVenteAproximite(rayon, myLatlng)
}
function nettoyerPointVentes() {
    infoWindow.close();
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers.length = 0;
}
function rechercherPointVenteAproximite(rayon, center) {
    nettoyerPointVentes();
    // todo on affiche aussi la position de l'internaute
    // creer un marker particulier pour lui
    creerMarker(center, 'la position de internaute');
    $.getJSON('/magasin/pointVente/recherchePointVente?latparam=' + center.lat() + '&lngparam=' + center.lng() + '&rayon=' + rayon, function (data) {
        var bounds = new google.maps.LatLngBounds();
        $.each(data, function (key, val) {
            var name = val.name;
            var address = val.address;
            var distance = parseFloat(val.distance);
            var latlng = new google.maps.LatLng(parseFloat(val.lat), parseFloat(val.lng));
            creerMarker(latlng, name, address, distance);
            bounds.extend(latlng);
        });
        map.fitBounds(bounds);
    });
}
function creerMarker(latlng, name, address, distance) {
    var html = '<b>' + name + '</b> <br/>' + address + '</b> <br/> <b> distance : </b> ' + distance;
    var marker = new google.maps.Marker({
        map: map,
        position: latlng,
        title: name
    });
    google.maps.event.addListener(marker, 'click', function () {
        infoWindow.setContent(html);
        infoWindow.open(map, marker);
    });
    markers.push(marker);
}
