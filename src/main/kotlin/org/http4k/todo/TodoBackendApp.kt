package org.http4k.todo

import org.http4k.contract.Root
import org.http4k.contract.Route
import org.http4k.contract.RouteModule
import org.http4k.contract.SimpleJson
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.OPTIONS
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.DebuggingFilters
import org.http4k.format.Jackson
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main(args: Array<String>) {
    val port = if (args.isNotEmpty()) args[0] else "5000"
    val baseUrl = if (args.size > 1) args[1] else "http://localhost:$port"
    val todos = TodoDatabase(baseUrl)

    val filter = DebuggingFilters.PrintRequestAndResponse().then(Cors)

    val todoLens = Body.auto<TodoEntry>().required()
    val todoListLens = Body.auto<List<TodoEntry>>().required()

    fun lookup(id: String): HttpHandler = { todos.find(id)?.let { Response(OK).with(todoLens to it) } ?: Response(NOT_FOUND) }
    fun patch(id: String): HttpHandler = { Response(OK).with(todoLens to todos.save(id, todoLens(it))) }
    fun delete(id: String): HttpHandler = { todos.delete(id)?.let { Response(OK).with(todoLens to it) } ?: Response(NOT_FOUND) }
    fun list(): HttpHandler = { Response(OK).with(todoListLens to todos.all()) }
    fun clear(): HttpHandler = { Response(OK).with(todoListLens to todos.clear()) }
    fun save(): HttpHandler = { Response(OK).with(todoLens to todos.save(null, todoLens(it))) }

    RouteModule(Root, SimpleJson(Jackson), filter)
        .withRoute(Route().at(GET) / Path.of("id") bind ::lookup)
        .withRoute(Route().at(PATCH) / Path.of("id") bind ::patch)
        .withRoute(Route().at(DELETE) / Path.of("id") bind ::delete)
        .withRoute(Route().at(OPTIONS) bind { Response(OK) })
        .withRoute(Route().at(GET) bind list())
        .withRoute(Route().at(POST) bind save())
        .withRoute(Route().at(DELETE) bind clear())
        .toHttpHandler()
        .asServer(Jetty(port.toInt())).start().block()
}

data class TodoEntry(val id: String? = null, val url: String? = null, val title: String? = null, val order: Int? = 0, val completed: Boolean? = false)
