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
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Stepwise
import spock.lang.Unroll

import javax.inject.Inject
import java.time.LocalDate

import static com.tgt.backpackelasticsearch.test.DataProvider.getHeaders

@MicronautTest
@Stepwise
class SearchRegistrySortingFunctionalTest extends BaseElasticFunctionalTest {

    String uri = BackpackElasticsearchConstants.ELASTIC_SEARCH_BASEPATH

    @Inject
    CreateRegistryService createRegistryService

    @Inject
    GetRegistryService getRegistryService

    @Unroll
    def "test create new list with name "() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", RegistryType.WEDDING,
            RegistryStatus.@ACTIVE, RegistrySearchVisibility.@PUBLIC, registrantFirst, registrantLast, coregistrantFirst,
            coregistrantLast, eventState, "city" , state, "USA", eventDate, null, null, null, null)

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        state    | registrantFirst    |   registrantLast  |   coregistrantFirst   |   coregistrantLast | eventState  | eventDate
         "MN"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization1"  | LocalDate.of(2021, 01, 01)
         "TX"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization2"  | LocalDate.of(2021, 02, 01)
         "CA"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization3"  | LocalDate.of(2021, 03, 01)
         "DE"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization4"  | LocalDate.of(2021, 04, 01)
         "FL"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization5"  | LocalDate.of(2021, 05, 01)
         "WI"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization6"  | LocalDate.of(2020, 01, 01)
         "UT"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization7"  | LocalDate.of(2020, 02, 01)
         "OH"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization8"  | LocalDate.of(2020, 03, 01)
         "MO"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization9"  | LocalDate.of(2020, 04, 01)
         "MT"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization10" | LocalDate.of(2020, 05, 01)
         "IA"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization11" | LocalDate.of(2020, 06, 01)
         "KY"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization12" | LocalDate.of(2020, 07, 01)
         "LA"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization13" | LocalDate.of(2020, 07, 30)
         "NJ"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization14" | LocalDate.of(2020, 07, 29)
         "GA"    | "first name"       |   "last name"     |   "co first"         |    "co last"       |  "organization15" | LocalDate.of(2020, 07, 28)
    }

    def "test sort based on field location and order asc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&sort_field=location&sort_order=asc"

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
        actual[0].eventState == "CA"
        actual[1].eventState == "DE"
        actual[2].eventState == "FL"
        actual[3].eventState == "GA"
        actual[4].eventState == "IA"
        actual[5].eventState == "KY"
        actual[6].eventState == "LA"
        actual[7].eventState == "MN"
        actual[8].eventState == "MO"
        actual[9].eventState == "MT"
        actual[10].eventState == "NJ"
        actual[11].eventState == "OH"
        actual[12].eventState == "TX"
        actual[13].eventState == "UT"
        actual[14].eventState == "WI"
    }

    def "test sort based on field location and order desc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&sort_field=location&sort_order=desc"

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
        actual[14].eventState == "CA"
        actual[13].eventState == "DE"
        actual[12].eventState == "FL"
        actual[11].eventState == "GA"
        actual[10].eventState == "IA"
        actual[9].eventState == "KY"
        actual[8].eventState == "LA"
        actual[7].eventState == "MN"
        actual[6].eventState == "MO"
        actual[5].eventState == "MT"
        actual[4].eventState == "NJ"
        actual[3].eventState == "OH"
        actual[2].eventState == "TX"
        actual[1].eventState == "UT"
        actual[0].eventState == "WI"
    }

    def "test sort based on field event_date and order asc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&sort_field=event_date&sort_order=asc"

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
        actual[0].organizationName == "organization6"
        actual[1].organizationName == "organization7"
        actual[2].organizationName == "organization8"
        actual[3].organizationName == "organization9"
        actual[4].organizationName == "organization10"
        actual[5].organizationName == "organization11"
        actual[6].organizationName == "organization12"
        actual[7].organizationName == "organization15"
        actual[8].organizationName == "organization14"
        actual[9].organizationName == "organization13"
        actual[10].organizationName == "organization1"
        actual[11].organizationName == "organization2"
        actual[12].organizationName == "organization3"
        actual[13].organizationName == "organization4"
        actual[14].organizationName == "organization5"
    }

    def "test sort based on field event_date and order desc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=first&last_name=last&channel=WEB&sub_channel=TGTWEB&sort_field=event_date&sort_order=desc"

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
        actual[0].organizationName == "organization5"
        actual[1].organizationName == "organization4"
        actual[2].organizationName == "organization3"
        actual[3].organizationName == "organization2"
        actual[4].organizationName == "organization1"
        actual[5].organizationName == "organization13"
        actual[6].organizationName == "organization14"
        actual[7].organizationName == "organization15"
        actual[8].organizationName == "organization12"
        actual[9].organizationName == "organization11"
        actual[10].organizationName == "organization10"
        actual[11].organizationName == "organization9"
        actual[12].organizationName == "organization8"
        actual[13].organizationName == "organization7"
        actual[14].organizationName == "organization6"
    }

    @Unroll
    def "test create new list with name - 2"() {
        given:
        RegistryData registryRequest = new RegistryData(UUID.randomUUID(), "interesting title", RegistryType.WEDDING,
            RegistryStatus.@ACTIVE, RegistrySearchVisibility.@PUBLIC, registrantFirst, registrantLast, coregistrantFirst,
            coregistrantLast, eventState, "city" , state, "USA", eventDate, null, null, null, null)

        when:
        def response = createRegistryService.saveRegistry(registryRequest).block()

        then:
        def actualStatus = response.v1()
        actualStatus != null

        where:
        state    | registrantFirst    |   registrantLast  |   coregistrantFirst   |   coregistrantLast | eventState        | eventDate
        "MN"    | "Alice A"           |   "Bob"           |   "co first"          |    "co last"       |  "organization1"  | LocalDate.now().plusDays(14)
        "TX"    | "Tim"               |   "Alice B"       |   "co first"          |    "co last"       |  "organization2"  | LocalDate.now().plusDays(13)
        "CA"    | "Jen"               |   "Alice C"       |      "co first"       |    "co last"       |  "organization3"  | LocalDate.now().plusDays(9)
        "DE"    | "Alice D"           |   "last name"     |   "co first"          |    "co last"       |  "organization4"  | LocalDate.now().plusDays(12)
        }

    def "test sort based on name and order asc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=Alice&last_name=A&channel=WEB&sub_channel=TGTWEB&sort_field=name&sort_order=asc"

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
        actual.size() == 4
        actual[0].registrantFirstName == "Alice A"
        actual[1].registrantFirstName == "Alice D"
        actual[2].registrantFirstName == "Jen"
        actual[3].registrantFirstName == "Tim"

    }

    def "test sort based on name and order desc"() {
        given:
        String guestId = "1236"
        def url = uri + "?first_name=Alice&last_name=A&channel=WEB&sub_channel=TGTWEB&sort_field=name&sort_order=desc"

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
        actual.size() == 4
        actual[0].registrantFirstName == "Tim"
        actual[1].registrantFirstName == "Jen"
        actual[2].registrantFirstName == "Alice D"
        actual[3].registrantFirstName == "Alice A"

    }
}
