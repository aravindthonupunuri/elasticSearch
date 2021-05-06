package com.tgt.backpackelasticsearch.api

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.backpackelasticsearch.test.BaseElasticFunctionalTest
import com.tgt.backpackelasticsearch.transport.PaginatedRegistryData
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.backpackregistryclient.util.RegistrySearchVisibility
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate

import static com.tgt.backpackelasticsearch.test.DataProvider.getHeaders

@MicronautTest
@Stepwise
class SearchRegistryPaginationFunctionalTest extends BaseElasticFunctionalTest {

    String uri = BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH

    @Inject
    CreateRegistryService createRegistryService

    @Inject
    GetRegistryService getRegistryService

    @Unroll
    def "test create new list with name "() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", registryType, registryStatus, searchVisibility, registrantFirst, registrantLast, coregistrantFirst, coregistrantLast, organizationName, "MSP", "MN", "USA", LocalDate.now(), null, null, null, null)

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        registryType            | registryStatus             | registrantFirst   |   registrantLast  |   coregistrantFirst   |   coregistrantLast | organizationName  | searchVisibility
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization1"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization2"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"      |   "co first"        |    "co last"       |  "organization3"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization4"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization5"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization6"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization7"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization8"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization9"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization10" | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization11" | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization12" | RegistrySearchVisibility.@PUBLIC
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization13" | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization14" | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization15" | RegistrySearchVisibility.@PUBLIC

    }

    def "test get registry with page 1 and size 5"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&page=1&page_size=5"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<PaginatedRegistryData> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
            .headers(getHeaders(guestId)), PaginatedRegistryData)

        def actualStatus = listResponse.status()
        def actual = listResponse.body().registryDataList

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 5
        listResponse.body().currentPage == 1
        listResponse.body().pageSize == 5
        listResponse.body().totalRecords == 15
        actual[0].organizationName == "organization1"
        actual[1].organizationName == "organization2"
        actual[2].organizationName == "organization3"
        actual[3].organizationName == "organization4"
        actual[4].organizationName == "organization5"
    }

    def "test get registry with page 2 and size 5"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&page=2&page_size=5"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<PaginatedRegistryData> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), PaginatedRegistryData)

        def actualStatus = listResponse.status()
        def actual = listResponse.body().registryDataList

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 5
        listResponse.body().currentPage == 2
        listResponse.body().pageSize == 5
        listResponse.body().totalRecords == 15
        actual[0].organizationName == "organization6"
        actual[1].organizationName == "organization7"
        actual[2].organizationName == "organization8"
        actual[3].organizationName == "organization9"
        actual[4].organizationName == "organization10"
    }

    def "test get registry with page 3 and size 5"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&page=3&page_size=5"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<PaginatedRegistryData> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), PaginatedRegistryData)

        def actualStatus = listResponse.status()
        def actual = listResponse.body().registryDataList

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 5
        listResponse.body().currentPage == 3
        listResponse.body().pageSize == 5
        listResponse.body().totalRecords == 15
        actual[0].organizationName == "organization11"
        actual[1].organizationName == "organization12"
        actual[2].organizationName == "organization13"
        actual[3].organizationName == "organization14"
        actual[4].organizationName == "organization15"
    }

    def "test get registry with default page and size"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<PaginatedRegistryData> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), PaginatedRegistryData)

        def actualStatus = listResponse.status()
        def actual = listResponse.body().registryDataList

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 15
        listResponse.body().currentPage == 1
        listResponse.body().pageSize == 28
        listResponse.body().totalRecords == 15
    }

    def "test get registry with 0 page - bad request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&page=0"

        when:
        client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), PaginatedRegistryData)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }
}
