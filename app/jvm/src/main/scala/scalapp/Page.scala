package scalapp

import scalatags.Text.all._

object Page {
  val boot =
    "scalapp.Client().main()"
  val skeleton =
    html(
      head(
        link(
          rel := "stylesheet",
          href := "https://cdn.jsdelivr.net/picnicss/6.1.4/picnic.min.css"),
        link(
          rel := "stylesheet",
          href := "/app-styles.css")),
      body(
        onload := boot,
        div(id := "contents", "Loading..."),
        script(src := "/app-jsdeps.js"),
        script(src := "/app-fastopt.js")))
}
