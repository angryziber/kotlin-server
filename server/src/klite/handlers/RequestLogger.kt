package klite.handlers

import klite.*

typealias RequestLogFormatter = HttpExchange.(ms: Long) -> String?
val defaultRequestLogFormatter: RequestLogFormatter = { ms ->
  "$remoteAddress $method $path$query: $statusCode in $ms ms - $browser" +
    (failure?.takeIf { it !is StatusCodeException }?.let { " - $it" } ?: "")
}

open class RequestLogger(
  val formatter: RequestLogFormatter = defaultRequestLogFormatter
): Decorator {
  private val log = logger()

  override suspend fun invoke(exchange: HttpExchange, handler: Handler): Any? {
    val start = System.nanoTime()
    exchange.onComplete {
      val ms = (System.nanoTime() - start) / 1000_000
      formatter(exchange, ms)?.let { log.info(it) }
    }
    return handler(exchange)
  }
}
