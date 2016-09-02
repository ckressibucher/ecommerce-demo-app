package scalapp

import scalatags.Text.all._

object Page {
  val boot =
    "scalapp.Client().main(document.getElementById('contents'))"
  val skeleton =
    html(
      head(
        script(src:="/app-fastopt.js"),
        link(
          rel:="stylesheet",
          href := "https://cdn.jsdelivr.net/picnicss/6.1.4/picnic.min.css"
          //href:="http://yui.yahooapis.com/pure/0.5.0/pure-min.css"
        )
      ),
      body(
        onload:=boot,
        div(id:="contents")
      )
    )
}
