package quotations;

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
public class QuoteController {

	WebClient client;
	ClientRequest request;
	ClientHttpConnector clientHttpConnector;

	@GetMapping("/randomquotation")
	Mono<Quote> getQuote() {

		return WebClient.create()
		.get()
		.uri("https://gturnquist-quoters.cfapps.io/api/random")
		.accept(MediaType.APPLICATION_JSON)
		.retrieve()
		.bodyToMono(Quote.class);
	}


	@GetMapping("/quotes")
	Flux<Quote> getQuotes(@RequestParam(name="startQuotation", defaultValue="1", required=false) int startQuotation,
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

		Mono<Quote> firstMono = WebClient.create()
				.get()
				.uri(uriString)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Quote.class);

		Flux<Quote> flux = firstMono.flux();
		
		if (numQuotations > 1) {

			for (int i = 1; i < numQuotations; i++) {

				nextQuotation = nextQuotation + 1;

				if (nextQuotation > 12) {
					nextQuotation = 1;
				}

				uriString = "https://gturnquist-quoters.cfapps.io/api/" + nextQuotation;

				Mono<Quote> nextMono = WebClient.create()
						.get()
						.uri(uriString)
						.accept(MediaType.APPLICATION_JSON)
						.retrieve()
						.bodyToMono(Quote.class);

				flux = flux.concatWith(nextMono);
			}
		}

		return flux;
	}
}
