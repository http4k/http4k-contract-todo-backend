package org.http4k.todo

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.OPTIONS
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header

object Cors : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        {
            val response = if (it.method == OPTIONS) Response(OK) else next(it)
            response.with(
                Header.required("access-control-allow-origin") to "*",
                Header.required("access-control-allow-headers") to "content-type",
                Header.required("access-control-allow-methods") to "POST, GET, OPTIONS, PUT, PATCH,  DELETE"
            )
        }
}


