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
import io.micronaut.test.annotation.MicronautTest
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
        def url = uri + "?first_name=" + firstName + "&last_name=" + lastName + "&organization_name" + organizationName + "&channel=WEB&sub_channel=KIOSK"

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
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", registryType, registryStatus, searchVisibility, registrantFirst, registrantLast, coregistrantFirst, coregistrantLast, organizationName, "MSP", "MN", "USA", LocalDate.now())

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        registryType            | registryStatus             | registrantFirst   |   registrantLast  |   coregistrantFirst   |   coregistrantLast | organizationName  | searchVisibility
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization1"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization 2" | RegistrySearchVisibility.@PRIVATE
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "fist name"       |   "last one"      |   "co first"          |   "co last"        |  "organization3"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "funny first"     |   "seri last"     |   "last first"        |   "first am"       |  "organization4"  | RegistrySearchVisibility.@PRIVATE
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "sdkw wef"        |   "opwe sd23"     |   "wegs whwe"         |   "hkn,de she"     |  "organization5"  | RegistrySearchVisibility.@PUBLIC
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "kbnch sgs"       |   "sdfklkxcn cs"  |   "sgsd bsd"          |   "sgds, cmf"      |  "organization 6" | RegistrySearchVisibility.@PUBLIC
    }

    def "test get registry by first, last name - valid request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=KIOSK"

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
        actual.size() == 1
    }

    def "test get registry by organizationName"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&organization_name=organization5&channel=WEB&sub_channel=KIOSK"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 1
        actual.first().organizationName == "organization5"
    }

    def "test get registry when both name and organization are passed in request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&organization_name=organization1&channel=WEB&sub_channel=KIOSK"

        when:
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 1
        actual.first().organizationName == "organization1"
    }

    def "test get registry when channel is not passed"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&organization_name=organization1&sub_channel=KIOSK"

        when:
        client.toBlocking().exchange(HttpRequest.GET(url).headers (getHeaders(guestId)), RegistryData[])

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }

    def "test get registry gives empty response for Inactive registry"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=kbnch&last_name=sdfklkxcn&channel=WEB&sub_channel=KIOSK"

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
        def url = uri + "?first_name=funny&last_name=seri&channel=WEB&sub_channel=KIOSK"

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
}
