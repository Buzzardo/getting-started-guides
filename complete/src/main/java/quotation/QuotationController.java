package quotation;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class QuotationController {

	WebClient client;
	ClientRequest request;
	ClientHttpConnector clientHttpConnector;

	@GetMapping("/randomquotation")
	Mono<Quotation> getQuote() {

		return getQuotation("https://gturnquist-quoters.cfapps.io/api/random");
	}


	@GetMapping("/quotations")
	Flux<Quotation> getQuotes(@RequestParam(name="startQuotation", defaultValue="1", required=false) int startQuotation,
						  @RequestParam(name="numQuotations", defaultValue="1", required=false) int numQuotations) {

		if (startQuotation > 12) {
			startQuotation = startQuotation % 12; // The quotation service has 12 entries.
		}

		if (startQuotation < 1) {
			startQuotation = 1;
		}

		if (numQuotations > 12) {
			numQuotations = 12;
		}

		if (numQuotations < 1) {
			numQuotations = 1;
		}
		
		int nextQuotation = startQuotation;

		String uriString = "https://gturnquist-quoters.cfapps.io/api/" + nextQuotation;

		Mono<Quotation> firstMono = getQuotation(uriString);

		Flux<Quotation> flux = firstMono.flux();
		
		if (numQuotations > 1) {

			for (int i = 1; i < numQuotations; i++) {

				nextQuotation = nextQuotation + 1;

				if (nextQuotation > 12) {
					nextQuotation = 1;
				}

				uriString = "https://gturnquist-quoters.cfapps.io/api/" + nextQuotation;

				Mono<Quotation> nextMono = getQuotation(uriString);

				flux = flux.concatWith(nextMono);
			}
		}

		return flux;
	}

	private Mono<Quotation> getQuotation(String uri) {

		Mono<Quotation> mono = WebClient.create()
				.get()
				.uri(uri)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Quotation.class);

		return mono;
	}
}
