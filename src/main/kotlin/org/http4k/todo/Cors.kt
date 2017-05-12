package org.http4k.todo

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.with
import org.http4k.lens.Header

object Cors : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        {
            next(it).with(
                Header.required("access-control-allow-origin") to "*",
                Header.required("access-control-allow-headers") to "content-type",
                Header.required("access-control-allow-methods") to "POST, GET, OPTIONS, PUT, PATCH,  DELETE"
            )
        }
}


