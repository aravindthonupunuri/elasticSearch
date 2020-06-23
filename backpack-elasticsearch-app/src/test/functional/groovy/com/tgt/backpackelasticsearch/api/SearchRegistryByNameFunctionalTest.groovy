package com.tgt.backpackelasticsearch.api


import com.tgt.backpackelasticsearch.test.BaseKafkaFunctionalTest
import com.tgt.backpackelasticsearch.transport.RegistryData
import com.tgt.backpackelasticsearch.util.BackpackElasticsearchConstants
import com.tgt.backpackregistry.util.RegistryChannel
import com.tgt.backpackregistry.util.RegistrySubChannel
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Unroll

import static com.tgt.backpackelasticsearch.test.DataProvider.getHeaders

@MicronautTest
class SearchRegistryByNameFunctionalTest extends BaseKafkaFunctionalTest {

    String uri = BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH

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

//    def "test get registry by first, last name - valid request"() {
//        given:
//        String guestId = "1236"
//        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=KIOSK"
//
//        when:
//        HttpResponse<List<RegistryData>> listResponse = client.toBlocking()
//            .exchange(HttpRequest.GET(url)
//                .headers (getHeaders(guestId)), RegistryData)
//
//        def actualStatus = listResponse.status()
//        def actual = listResponse.body()
//
//        then:
//        actualStatus == HttpStatus.OK
//    }
}
