let map;
function initMap() {
    const container = document.getElementById('map');
    const options = { center: new kakao.maps.LatLng(35.1411, 126.8910), level: 3 }; // 광주역 대략 좌표(예시)
    map = new kakao.maps.Map(container, options);
}

document.addEventListener('DOMContentLoaded', function() {
    initMap();

    document.getElementById('searchBtn').addEventListener('click', async () => {
        const station = document.getElementById('stationInput').value;
        const res = await fetch(`/api/places/nearby?station=${encodeURIComponent(station)}`);
        if (!res.ok) { alert('데이터 조회 실패'); return; }
        const places = await res.json();
        renderPlaces(places);
    });
});

function renderPlaces(places) {
    const list = document.getElementById('places');
    list.innerHTML = '';
    // 지도 위 기존 마커 제거는 생략 - 실제 구현 시 관리 배열로 제거 필요
    places.forEach(p => {
        const li = document.createElement('li');
        li.innerHTML = `<strong>${p.placeName}</strong><br>${p.distance || ''}<br>
      <a href="https://map.kakao.com/?q=${encodeURIComponent(p.placeName)}" target="_blank">카카오맵에서 열기</a> |
      <a href="${buildRouteUrl(p)}" target="_blank">길찾기</a>`;
        list.appendChild(li);

        if (p.latitude && p.longitude) {
            const marker = new kakao.maps.Marker({
                map: map,
                position: new kakao.maps.LatLng(p.latitude, p.longitude),
                title: p.placeName
            });
        }
    });
}

// 카카오 길찾기 URL 빌드 (웹 가이드에 따른 간단한 예)
function buildRouteUrl(place) {
    // 카카오 지도 길찾기 URL parameter: 'from' 'to' 등 사용 or 장소명 q 파라미터
    // 좀 더 정밀한 길찾기는 카카오내비 API 또는 route URL 스킴 참고
    return `https://map.kakao.com/link/to/${encodeURIComponent(place.placeName)},${place.latitude},${place.longitude}`;
}
