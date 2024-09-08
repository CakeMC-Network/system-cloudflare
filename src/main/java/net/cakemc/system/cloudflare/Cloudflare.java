package net.cakemc.system.cloudflare;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public final class Cloudflare {
	private static final String
		 HEADER_CONTENT = "Content-Type",
		 HEADER_CONTENT_VALUE = "application/json",
		 HEADER_EMAIL = "X-Auth-Email",
		 HEADER_EMAIL_VALUE = "<your-email>",
		 HEADER_KEY = "X-Auth-Key",
		 HEADER_KEY_VALUE = "<your-api-key>";

	private static final String
		 BODY = """
		        {
		          "comment": "Domain verification record",
		          "name": "%s",
		          "proxied": true,
		          "tags": [],
		          "ttl": 3600,
		          "content": "%s",
		          "type": "%s"
		        }""";

	private static final HttpClient CLIENT =
		 HttpClient.newBuilder()
		           .version(Version.HTTP_2)
		           .proxy(ProxySelector.of(null))
		           .followRedirects(Redirect.ALWAYS)
		           .connectTimeout(Duration.of(10L, ChronoUnit.SECONDS))
		           .executor(Executors.newSingleThreadExecutor())
		           .build();

	public static void main(String... args) throws IOException {
		String ipv4 = ipv4(), ipv6 = ipv6();
		System.out.printf("[ipv4=%s]%n[ipv6=%s]%n", ipv4, ipv6);
		// TODO: ... | ip resolving behaviour unsafe, prolly should create a dyndns endpoint somehow
		if (true) return;

		Stream.of(
			 // cakemc.net
			 CLIENT.sendAsync(
				  HttpRequest.newBuilder()
				             .uri(URI.create("https://api.cloudflare.com/client/v4/zones/zone_id/dns_records/dns_record_id"))
				             .header(HEADER_CONTENT, HEADER_CONTENT_VALUE)
				             .header(HEADER_EMAIL, HEADER_EMAIL_VALUE)
				             .header(HEADER_KEY, HEADER_KEY_VALUE)
				             .method("PUT", ofBody("cakemc.net", "", Type.IPV4)).build(),
				  HttpResponse.BodyHandlers.ofString()
			 ),
			 CLIENT.sendAsync(
				  HttpRequest.newBuilder()
				             .uri(URI.create("https://api.cloudflare.com/client/v4/zones/zone_id/dns_records/dns_record_id"))
				             .header(HEADER_CONTENT, HEADER_CONTENT_VALUE)
				             .header(HEADER_EMAIL, HEADER_EMAIL_VALUE)
				             .header(HEADER_KEY, HEADER_KEY_VALUE)
				             .method("PUT", ofBody("cakemc.net", "", Type.IPV6)).build(),
				  HttpResponse.BodyHandlers.ofString()
			 ),

			 // repo.cakemc.net
			 CLIENT.sendAsync(
				  HttpRequest.newBuilder()
				             .uri(URI.create("https://api.cloudflare.com/client/v4/zones/zone_id/dns_records/dns_record_id"))
				             .header(HEADER_CONTENT, HEADER_CONTENT_VALUE)
				             .header(HEADER_EMAIL, HEADER_EMAIL_VALUE)
				             .header(HEADER_KEY, HEADER_KEY_VALUE)
				             .method("PUT", ofBody("repo.cakemc.net", "", Type.IPV4)).build(),
				  HttpResponse.BodyHandlers.ofString()
			 )
		).map(CompletableFuture::join).forEach(response -> {
			System.out.println(response.body());
		});
	}

	private static String ipv4() throws IOException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		}
	}

	private static String ipv6() throws IOException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("2001:4860:4860::8888"), 10002);
			return socket.getLocalAddress().getHostAddress();
		}
	}

	private static BodyPublisher ofBody(
		 String domain,
		 String ip,
		 Type type
	) {
		return HttpRequest.BodyPublishers.ofString(BODY.formatted(domain, ip, switch (type) {
			case IPV4 -> "A";
			case IPV6 -> "AAAA";
		}));
	}

	enum Type {IPV4, IPV6}
}
