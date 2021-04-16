package com.tgt.backpackelasticsearch.api

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.backpackelasticsearch.test.BaseElasticFunctionalTest
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import com.tgt.backpackregistryclient.util.RegistrySearchVisibility
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate

import static com.tgt.backpackelasticsearch.test.DataProvider.getHeaders

@MicronautTest
@Stepwise
class SearchRegistryByNameFunctionalTest extends BaseElasticFunctionalTest {

    String uri = BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH

    @Inject
    CreateRegistryService createRegistryService

    @Inject
    GetRegistryService getRegistryService

    @Unroll
    def "test get registry by first, last name - bad request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=" + firstName + "&last_name=" + lastName + "&organization_name" + organizationName + "&channel=WEB&sub_channel=TGTWEB"

        when:
        client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        where:
           firstName   |   lastName     | organizationName
           ""          |   "lastName"   | ""
          "firstName"  |   ""           | ""
           ""          |   ""           | ""
    }

    @Unroll
    def "test create new list with name "() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", registryType, registryStatus, searchVisibility, registrantFirst, registrantLast, coregistrantFirst, coregistrantLast, organizationName, "MSP", state, "USA", eventDate, "https://test-image-url", "1234-imageid", "0.20,0.10,0.60,0.80", "Target/ugc/206673282.tif")

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        registryType            | registryStatus             | registrantFirst   |   registrantLast  |   coregistrantFirst   |   coregistrantLast | organizationName  |  searchVisibility                   | state | eventDate
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization1"  | RegistrySearchVisibility.@PUBLIC    | "MN"  | LocalDate.of(2021, 01, 01)
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization1"  | RegistrySearchVisibility.@PUBLIC    | "WI"  | LocalDate.of(2021, 01, 01)
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization1"  | RegistrySearchVisibility.@PUBLIC    | "MN"  | LocalDate.of(2022, 01, 01)
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization 2" | RegistrySearchVisibility.@PRIVATE   | "PA"  | LocalDate.of(2022, 02, 02)
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization3"  | RegistrySearchVisibility.@PUBLIC    | "NY"  | LocalDate.of(2021,01,01)
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "funny first"     |   "seri last"     |   "last first"        |   "first am"       |  "organization4"  | RegistrySearchVisibility.@PRIVATE   | "CA"  | LocalDate.of(2021,02,01)
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "sdkw wef"        |   "opwe sd23"     |   "wegs whwe"         |   "hkn,de she"     |  "organization5"  | RegistrySearchVisibility.@PUBLIC    | "TX"  | LocalDate.of(2021, 02, 01)
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "kbnch sgs"       |   "sdfklkxcn cs"  |   "sgsd bsd"          |   "sgds, cmf"      |  "organization 6" | RegistrySearchVisibility.@PUBLIC    | "SD"  | LocalDate.of(2021, 02, 01)
    }

    def "test get registry by first, last name - valid request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=fi&last_name=l&channel=WEB&sub_channel=TGTWEB"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 3
        actual.first().imageUrl == "https://test-image-url"
        actual.first().imageId == "1234-imageid"
        actual.first().imageDimension == "0.20,0.10,0.60,0.80"
        actual.first().imageUrlParams == "Target/ugc/206673282.tif"
    }

    def "test get registry by partial first, last name - valid request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=TGTWEB"

        when:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 3
    }

    def "test get registry by organizationName and registry type"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&organization_name=organization5&registry_type=BABY&channel=WEB&sub_channel=TGTWEB"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 4
        actual.first().organizationName == "organization5"
        actual.first().registryType == RegistryType.BABY
    }

    def "test get registry when both name and organization are passed in request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&organization_name=organization1&channel=WEB&sub_channel=TGTWEB"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 4
        actual.first().organizationName == "organization1"
    }

    def "test get registry when channel is not passed"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&organization_name=organization1&sub_channel=TGTWEB"

        when:
        client.toBlocking().exchange(HttpRequest.GET(url).headers (getHeaders(guestId)), RegistryData[])

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }

    def "test get registry gives empty response for Inactive registry"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=kbnch&last_name=sdfklkxcn&channel=WEB&sub_channel=TGTWEB"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 0
    }

    def "test get registry gives empty response for Private registry"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=funny&last_name=seri&channel=WEB&sub_channel=TGTWEB"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 0
    }

    def "test get registry with min_date gives valid registries"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=TGTWEB&min_date=2020-12-01"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 3
    }

    def "test get registry with only max_date gives valid registries"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=TGTWEB&max_date=2022-12-01"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 3
    }

    def "test get registry with min_date and max_date gives valid registries"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=TGTWEB&min_date=2020-12-01&max_date=2021-12-01"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url.toString())
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 2
    }

    def "test get registry with event state"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=TGTWEB&state=MN"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 2
        actual.first().eventState == "MN"
    }

    def "test add new registry without image params to es and verify search response"() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "no image title", RegistryType.@BABY,
                RegistryStatus.@ACTIVE, RegistrySearchVisibility.@PUBLIC, "abc", "xyz",
                "def", "uvw", "organization1",
                "MSP", "MN", "USA", LocalDate.of(2021, 02, 01),
                null, null, null, null)

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        and:
        def refreshResponse = refresh()

        then:
        refreshResponse.status() == HttpStatus.OK

        and:
        String guestId = "1236"
        def url = uri + "?first_name=abc&last_name=xyz&channel=WEB&sub_channel=TGTWEB"
        HttpResponse<RegistryData[]> searchResponse = client.toBlocking()
                .exchange(HttpRequest.GET(url)
                        .headers (getHeaders(guestId)), RegistryData[])

        def searchStatus = searchResponse.status()
        def actual = searchResponse.body()

        then:
        searchStatus == HttpStatus.OK
        actual.size() == 1
        actual.first().imageUrl == null
    }
}
