package atdd.path.docs;

import atdd.AbstractDocumentationTest;
import atdd.path.application.FavoritePathService;
import atdd.path.application.FavoriteStationService;
import atdd.path.domain.Edge;
import atdd.path.domain.FavoritePath;
import atdd.path.domain.Line;
import atdd.path.domain.Station;
import atdd.path.web.FavoriteController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static atdd.TestConstant.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

@WebMvcTest(FavoriteController.class)
public class FavoriteDocumentationTest extends AbstractDocumentationTest {
    public static final String HEADER_NAME = "Authorization";

    @MockBean
    private FavoriteStationService favoriteStationService;

    @MockBean
    private FavoritePathService favoritePathService;

    @Test
    void addFavoriteStation() throws Exception {
        given(favoriteStationService.addFavoriteStation(anyString(), anyLong())).willReturn(FAVORITE_STATION_RESPONSE);

        //when
        ResultActions result = this.mockMvc.perform(post("/favorites/stations")
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .content(String.valueOf(1l))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated())
                .andDo(
                        document("favorites/addFavoriteStation",
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                ),
                                responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description(" The favorite station id"),
                                        fieldWithPath("owner").type(JsonFieldType.NUMBER).description(" The favorite station's owner"),
                                        fieldWithPath("station.id").type(JsonFieldType.NUMBER).description(" The station's id"),
                                        fieldWithPath("station.name").type(JsonFieldType.STRING).description(" The station's name"),
                                        fieldWithPath("station.lines[].id").type(JsonFieldType.NUMBER).description(" The line's id"),
                                        fieldWithPath("station.lines[].name").type(JsonFieldType.STRING).description(" The line's name")
                                )
                        ))
                .andDo(print());
    }

    @Test
    void findFavoriteStations() throws Exception {
        given(favoriteStationService.findAll(anyString())).willReturn(Arrays.asList(FAVORITE_STATION_RESPONSE));

        //when
        ResultActions result = this.mockMvc.perform(get("/favorites/stations")
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(
                        document("favorites/findFavoriteStations",
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                ),
                                responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description(" The favorite station id"),
                                        fieldWithPath("[].owner").type(JsonFieldType.NUMBER).description(" The favorite station's owner"),
                                        fieldWithPath("[].station.id").type(JsonFieldType.NUMBER).description(" The station's id"),
                                        fieldWithPath("[].station.name").type(JsonFieldType.STRING).description(" The station's name"),
                                        fieldWithPath("[].station.lines[].id").type(JsonFieldType.NUMBER).description(" The line's id"),
                                        fieldWithPath("[].station.lines[].name").type(JsonFieldType.STRING).description(" The line's name")
                                )
                        ))
                .andDo(print());

    }

    @Test
    void deleteFavoriteStation() throws Exception {
        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.delete("/favorites/stations/{id}", 1l)
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNoContent())
                .andDo(
                        document("favorites/deleteFavoriteStation",
                                pathParameters(
                                        parameterWithName("id").description(" The favorites station's id")
                                ),
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                )
                        ))
                .andDo(print());
    }

    @Test
    void addFavoritePath() throws Exception {
        FavoritePath favoritePath = givenFavoritePath();
        List<Edge> edges1 = Arrays.asList(TEST_EDGE, TEST_EDGE_2, TEST_EDGE_3, TEST_EDGE_4); //2호선
        List<Edge> edges2 = Arrays.asList(TEST_EDGE_5, TEST_EDGE_6, TEST_EDGE_7, TEST_EDGE_8); //신분당선

        Line line1 = new Line(LINE_ID, LINE_NAME, edges1, LocalTime.of(5, 45), LocalTime.of(00, 05), 10);
        Line line2 = new Line(LINE_ID_2, LINE_NAME_2, edges2, LocalTime.of(5, 45), LocalTime.of(00, 05), 10);

        Station station = new Station(STATION_ID, STATION_NAME, Arrays.asList(line1, line2));
        Station station2 = new Station(STATION_ID_4, STATION_NAME_4, Arrays.asList(line1));

        given(favoritePathService.findFavoritePath(anyLong())).willReturn(Arrays.asList(favoritePath));
        given(favoritePathService.findStationById(favoritePath.getSourceStationId())).willReturn(station);
        given(favoritePathService.findStationById(favoritePath.getTargetStationId())).willReturn(station2);

        given(favoritePathService.addFavoritePath(any())).willReturn(favoritePath);

        //when
        String input = "{\"sourceStationId\": 1, \"targetStationId\": 4}";

        ResultActions result = this.mockMvc.perform(post("/favorites/paths")
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated())
                .andDo(
                        document("favorites/addFavoritePath",
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                ),
                                requestFields(
                                        fieldWithPath("sourceStationId").type(JsonFieldType.NUMBER).description(" The favorite path source station's id"),
                                        fieldWithPath("targetStationId").type(JsonFieldType.NUMBER).description(" The favorite path target station's id")
                                ),
                                responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description(" The favorite path id"),
                                        fieldWithPath("paths[].startStationId").type(JsonFieldType.NUMBER).description(" The favorite path start station's id"),
                                        fieldWithPath("paths[].endStationId").type(JsonFieldType.NUMBER).description(" The favorite path end station's id"),
                                        fieldWithPath("paths[].stations[].id").type(JsonFieldType.NUMBER).description(" The favorite path station's id"),
                                        fieldWithPath("paths[].stations[].name").type(JsonFieldType.STRING).description(" The favorite path station's name"),
                                        fieldWithPath("paths[].stations[].lines[].id").type(JsonFieldType.NUMBER).description(" The station's line id"),
                                        fieldWithPath("paths[].stations[].lines[].name").type(JsonFieldType.STRING).description(" The station's line name")
                                )
                        ))
                .andDo(print());
    }

    @Test
    void findFavoritePaths() throws Exception {
        FavoritePath favoritePath = givenFavoritePath();

        List<Edge> edges1 = Arrays.asList(TEST_EDGE, TEST_EDGE_2, TEST_EDGE_3, TEST_EDGE_4); //2호선
        List<Edge> edges2 = Arrays.asList(TEST_EDGE_5, TEST_EDGE_6, TEST_EDGE_7, TEST_EDGE_8); //신분당선

        Line line1 = new Line(LINE_ID, LINE_NAME, edges1, LocalTime.of(5, 45), LocalTime.of(00, 05), 10);
        Line line2 = new Line(LINE_ID_2, LINE_NAME_2, edges2, LocalTime.of(5, 45), LocalTime.of(00, 05), 10);

        Station station = new Station(STATION_ID, STATION_NAME, Arrays.asList(line1, line2));
        Station station2 = new Station(STATION_ID_4, STATION_NAME_4, Arrays.asList(line1));

        given(favoritePathService.findFavoritePath(anyLong())).willReturn(Arrays.asList(favoritePath));
        given(favoritePathService.findStationById(favoritePath.getSourceStationId())).willReturn(station);
        given(favoritePathService.findStationById(favoritePath.getTargetStationId())).willReturn(station2);


        //when
        ResultActions result = this.mockMvc.perform(get("/favorites/paths")
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(
                        document("favorites/findFavoritePaths",
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                ),
                                responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description(" The favorite path id"),
                                        fieldWithPath("[].paths[].startStationId").type(JsonFieldType.NUMBER).description(" The favorite path start station's id"),
                                        fieldWithPath("[].paths[].endStationId").type(JsonFieldType.NUMBER).description(" The favorite path end station's id"),
                                        fieldWithPath("[].paths[].stations[].id").type(JsonFieldType.NUMBER).description(" The favorite path station's id"),
                                        fieldWithPath("[].paths[].stations[].name").type(JsonFieldType.STRING).description(" The favorite path station's name"),
                                        fieldWithPath("[].paths[].stations[].lines[].id").type(JsonFieldType.NUMBER).description(" The station's line id"),
                                        fieldWithPath("[].paths[].stations[].lines[].name").type(JsonFieldType.STRING).description(" The station's line name")
                                )
                        ))
                .andDo(print());

    }

    @Test
    void deleteFavoritePath() throws Exception {
        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.delete("/favorites/paths/{id}", 1l)
                .header(HEADER_NAME, "Bearer " + TEST_USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNoContent())
                .andDo(
                        document("favorites/deleteFavoritePath",
                                requestHeaders(
                                        headerWithName(HEADER_NAME).description("Bearer auth credentials")
                                ),
                                pathParameters(
                                        parameterWithName("id").description(" The favorites paths's id")
                                )
                        ))
                .andDo(print());

    }

    private FavoritePath givenFavoritePath() {
        FavoritePath favoritePath = FAVORITE_PATH_1;

        return favoritePath;
    }
}