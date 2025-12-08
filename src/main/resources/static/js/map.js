// 전역 변수: 지도 객체와 마커 배열
let map;
let markers = []; // 마커 관리 배열 (중복 방지)

/**
 * 카카오 지도 초기화
 * - 광주역 중심으로 지도 생성 (위도 35.1411, 경도 126.8910)
 * - ⚠️ 이 함수는 kakao.maps.load()에서 자동 호출됩니다
 */
function initMap() {
    // Kakao Maps API가 로드되었는지 확인
    if (typeof kakao === 'undefined' || typeof kakao.maps === 'undefined') {
        console.error('❌ Kakao Maps API가 로드되지 않았습니다.');
        alert('지도를 불러올 수 없습니다. 카카오 JavaScript 키를 확인해주세요.');
        return;
    }

    const container = document.getElementById('map');
    const options = {
        center: new kakao.maps.LatLng(35.1411, 126.8910), // 광주역 좌표
        level: 5 // 지도 확대 레벨 (숫자가 클수록 넓은 범위)
    };
    map = new kakao.maps.Map(container, options);
    console.log('✅ 카카오 지도 초기화 완료');
}

/**
 * 기존 마커 모두 제거
 */
function clearMarkers() {
    markers.forEach(marker => marker.setMap(null));
    markers = [];
}

/**
 * 검색 결과를 리스트와 지도에 표시
 * @param {Array} places - 문화공간 목록 (CulturePlaceDto 배열)
 */
function renderPlaces(places) {
    const list = document.getElementById('places');
    list.innerHTML = ''; // 기존 리스트 초기화
    clearMarkers(); // 기존 마커 제거

    if (places.length === 0) {
        list.innerHTML = '<li>검색 결과가 없습니다.</li>';
        return;
    }

    places.forEach(place => {
        // 리스트 항목 생성
        const li = document.createElement('li');
        li.innerHTML = `
            <strong>${place.placeName}</strong>
            ${place.category ? `<span class="category">[${place.category}]</span>` : ''}<br>
            <small>${place.address || ''}</small><br>
            ${place.distance ? `<small class="distance">📍 ${Math.round(place.distance)}m</small><br>` : ''}
            <a href="https://map.kakao.com/?q=${encodeURIComponent(place.placeName)}" target="_blank">카카오맵에서 보기</a>
            ${place.latitude && place.longitude ?
            ` | <a href="https://map.kakao.com/link/to/${encodeURIComponent(place.placeName)},${place.latitude},${place.longitude}" target="_blank">길찾기</a>`
            : ''}
        `;
        list.appendChild(li);

        // 지도에 마커 추가
        if (place.latitude && place.longitude) {
            const markerPosition = new kakao.maps.LatLng(place.latitude, place.longitude);
            const marker = new kakao.maps.Marker({
                map: map,
                position: markerPosition,
                title: place.placeName
            });
            markers.push(marker); // 마커 배열에 저장

            // 인포윈도우 (마커 클릭 시 정보 표시)
            const infowindow = new kakao.maps.InfoWindow({
                content: `<div style="padding:5px;font-size:12px;">${place.placeName}</div>`
            });

            kakao.maps.event.addListener(marker, 'click', function() {
                infowindow.open(map, marker);
            });

            // 첫 번째 마커로 지도 중심 이동
            if (markers.length === 1) {
                map.setCenter(markerPosition);
            }
        }
    });
}

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('📄 DOM 로드 완료');

    // ⚠️ initMap()은 더 이상 여기서 호출하지 않습니다
    // body의 onload="kakao.maps.load(initMap)"에서 자동 호출됩니다

    // 검색 버튼 클릭 이벤트
    document.getElementById('searchBtn').addEventListener('click', async () => {
        const station = document.getElementById('stationInput').value.trim();

        if (!station) {
            alert('역명을 입력해주세요.');
            return;
        }

        try {
            // 1차: 광주교통공사 API 호출
            const res = await fetch(`/api/places/nearby?station=${encodeURIComponent(station)}`);

            if (!res.ok) {
                const errorMsg = await res.text();
                throw new Error(errorMsg || '데이터 조회 실패');
            }

            const places = await res.json();

            // 데이터가 없으면 카카오 API로 대체 검색
            if (places.length === 0) {
                console.log('광주교통공사 API 결과 없음. 카카오 API로 재검색...');
                await searchWithKakao(station);
            } else {
                renderPlaces(places);
            }

        } catch (error) {
            alert('오류 발생: ' + error.message);
            console.error(error);
        }
    });

    // 엔터키로도 검색 가능하도록 설정
    document.getElementById('stationInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            document.getElementById('searchBtn').click();
        }
    });
});

/**
 * 카카오 로컬 API로 대체 검색
 * - 광주교통공사 API에 데이터가 없을 때 사용
 */
async function searchWithKakao(station) {
    try {
        const res = await fetch(`/api/places/search-kakao?keyword=문화&station=${encodeURIComponent(station)}&radius=2000`);

        if (!res.ok) {
            throw new Error('카카오 검색 실패');
        }

        const places = await res.json();

        // 카카오 API 응답을 CulturePlaceDto 형식으로 변환
        const converted = places.map(p => ({
            placeName: p.name,
            address: p.address || p.roadAddress,
            latitude: p.y,
            longitude: p.x,
            category: p.category || '문화시설',
            distance: null // 카카오 API는 거리 정보 미제공
        }));

        renderPlaces(converted);

    } catch (error) {
        alert('카카오 검색도 실패했습니다: ' + error.message);
        console.error(error);
    }
}