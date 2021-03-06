package nextstep.subway.maps.map.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.maps.line.dto.LineResponse;
import nextstep.subway.maps.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static nextstep.subway.maps.line.acceptance.step.LineAcceptanceStep.지하철_노선_등록되어_있음;
import static nextstep.subway.maps.line.acceptance.step.LineStationAcceptanceStep.지하철_노선에_지하철역_등록되어_있음;
import static nextstep.subway.maps.map.acceptance.step.MapAcceptanceStep.*;
import static nextstep.subway.maps.station.acceptance.step.StationAcceptanceStep.지하철역_등록되어_있음;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("지하철 노선에 역 등록 관련 기능")
public class MapAcceptanceTest extends AcceptanceTest {
    private Long lineId1;
    private Long lineId2;
    private Long stationId1;
    private Long stationId2;
    private Long stationId3;
    private Long stationId4;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        ExtractableResponse<Response> createLineResponse1 = 지하철_노선_등록되어_있음("2호선", "GREEN");
        ExtractableResponse<Response> createLineResponse2 = 지하철_노선_등록되어_있음("신분당성", "RED");
        ExtractableResponse<Response> createdStationResponse1 = 지하철역_등록되어_있음("강남역");
        ExtractableResponse<Response> createdStationResponse2 = 지하철역_등록되어_있음("역삼역");
        ExtractableResponse<Response> createdStationResponse3 = 지하철역_등록되어_있음("선릉역");
        ExtractableResponse<Response> createdStationResponse4 = 지하철역_등록되어_있음("양재역");

        lineId1 = createLineResponse1.as(LineResponse.class).getId();
        lineId2 = createLineResponse2.as(LineResponse.class).getId();
        stationId1 = createdStationResponse1.as(StationResponse.class).getId();
        stationId2 = createdStationResponse2.as(StationResponse.class).getId();
        stationId3 = createdStationResponse3.as(StationResponse.class).getId();
        stationId4 = createdStationResponse4.as(StationResponse.class).getId();

        지하철_노선에_지하철역_등록되어_있음(lineId1, null, stationId1);
        지하철_노선에_지하철역_등록되어_있음(lineId1, stationId1, stationId2);
        지하철_노선에_지하철역_등록되어_있음(lineId1, stationId2, stationId3);
        지하철_노선에_지하철역_등록되어_있음(lineId2, null, stationId1);
        지하철_노선에_지하철역_등록되어_있음(lineId2, stationId1, stationId4);
    }

    @DisplayName("지하철 노선도를 조회한다.")
    @Test
    void loadMap() {
        // when
        ExtractableResponse<Response> response = 지하철_노선도_조회_요청();

        // then
        지하철_노선도_응답됨(response);
        지하철_노선도_노선별_지하철역_순서_정렬됨(response, lineId1, Arrays.asList(stationId1, stationId2, stationId3));
        지하철_노선도_노선별_지하철역_순서_정렬됨(response, lineId2, Arrays.asList(stationId1, stationId4));
    }

    @DisplayName("캐시 적용을 검증한다.")
    @Test
    void loadMapWithETag() {
        ExtractableResponse<Response> response = 지하철_노선도_조회_요청();

        String eTag = response.header("ETag");
        RestAssured.given().log().all().
                header("If-None-Match", eTag).
                accept(MediaType.APPLICATION_JSON_VALUE).
                when().
                get("/maps").
                then().
                statusCode(HttpStatus.NOT_MODIFIED.value()).
                header("ETag", notNullValue()).
                log().all().
                extract();
    }
}
