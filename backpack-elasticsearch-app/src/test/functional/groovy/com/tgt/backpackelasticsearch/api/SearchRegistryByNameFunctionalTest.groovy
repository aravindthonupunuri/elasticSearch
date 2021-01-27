package com.tgt.backpackelasticsearch.api

import com.tgt.backpackelasticsearch.service.GetRegistryService
import com.tgt.backpackelasticsearch.service.async.CreateRegistryService
import com.tgt.backpackelasticsearch.test.BaseElasticFunctionalTest
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistryStatus
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
        def url = uri + "?first_name=" + firstName + "&last_name=" + lastName + "&channel=" + registryChannel + "&sub_channel=" + registrySubChannel

        when:
        client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData)

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST

        where:
        registryChannel     |   registrySubChannel       |   firstName   |   lastName
        null                |   RegistrySubChannel.KIOSK |   "firstName" |   "lastName"
        RegistryChannel.WEB |   null                     |   "firstName" |   "lastName"
        RegistryChannel.WEB |   null                     |   "firstName" |   "lastName"
        RegistryChannel.WEB |   RegistrySubChannel.KIOSK |   ""          |   "lastName"
        RegistryChannel.WEB |   RegistrySubChannel.KIOSK |   "firstName" |   ""
    }

    @Unroll
    def "test create new list with name "() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", registryType, registryStatus, registrantFirst, registrantLast, coregistrantFirst, coregistrantLast, "MSP", "MN", "USA", LocalDate.now())

        when:

        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        registryType            | registryStatus             | registrantFirst   |   registrantLast  |   coregistrantFirst   |   coregistrantLast
        RegistryType.BABY       | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "fist name"       |   "last one"      |   "co first"          |   "co last"
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "fist name"       |   "last one"      |   "co first"          |   "co last"
        RegistryType.WEDDING    | RegistryStatus.@ACTIVE     | "funny first"     |   "seri last"     |   "last first"        |   "first am"
        RegistryType.BABY       | RegistryStatus.@INACTIVE   | "sdkw wef"        |   "opwe sd23"     |   "wegs whwe"         |   "hkn,de she"
        RegistryType.WEDDING    | RegistryStatus.@INACTIVE   | "kbnch sgs"       |   "sdfklkxcn cs"  |   "sgsd bsd"          |   "sgds, cmf"
    }


    def "test get registry by first, last name - valid request"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=co&last_name=last&channel=WEB&sub_channel=KIOSK"

        when:
        sleep(1000)
        HttpResponse<RegistryData[]> listResponse = client.toBlocking()
            .exchange(HttpRequest.GET(url)
                .headers (getHeaders(guestId)), RegistryData[])

        def actualStatus = listResponse.status()
        def actual = listResponse.body()

        then:
        actualStatus == HttpStatus.OK
        actual.size() == 2
    }

    def "test get registry gives empty response for Inactive registry"() {
        given:
        String guestId = "1234"
        def url = uri + "?first_name=sdkw&last_name=opwe&channel=WEB&sub_channel=KIOSK"

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
