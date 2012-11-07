jq = jQuery

debugging = off

######
# ui #
######

colors = [
  "hsb(0, .75, .75)"
  "hsb(.8, .75, .75)"
  "hsb(.3, .75, .75)"
  "hsb(.6, .75, .75)"
  "hsb(.1, .75, .75)"
]

black = "hsb(0,0,0.9)"

##################
# sentence stuff #
##################

r = -1 # current raphael Paper 
numSentences = 0

makeDepBox = (w, h) -> r = Raphael("sentence"+numSentences, w, h)

curve = (x, y, xa, ya, xb, yb, xz, yz, color) -> 
  path = [["M", x, y], ["C", xa, ya, xb, yb, xz, yz]]
  r.set(r.path(path).attr({stroke: color, "stroke-width": 4, "stroke-linecap": "round"}))

halfcircleHeight = (x1,x2) ->
  xdelta = x2-x1
  bezierScale = 0.5
  return (xdelta * bezierScale)

halfcircle = (x1, x2, y, color, height) ->
  curve(x1 ,y, x1, y-height, x2, y-height, x2, y, color)

calcDeps = (deps, positions) ->
  ret = []
  for d in deps
    if d[0] > d[1] then d = [d[1],d[0]]
    if d[0] != -1
      tok1 = positions[d[0]].center
      tok2 = positions[d[1]].center
      ret = ret.concat([
        t1: tok1
        t2: tok2
        y: -1
        color: colors[Math.min(4, d[1]-d[0]-1)]
        height: halfcircleHeight(tok1,tok2)
      ])
  y = maxDepHeight(ret)
  for d in ret
    d.y = y
  return ret

maxDepHeight = (calculatedDeps) ->
  return Math.max.apply(null, calculatedDeps.map((d) -> d.height))

      # draw a line straight, black up from the root token
      #x = positions[d[1]].center
      #path = [["M", x, 100], ["L", x, 30]]
      #r.set(r.path(path)).attr({stroke: grey, "stroke-width": 4, "stroke-linecap": "round"})

drawDeps = (calculatedDeps) ->
  for d in calculatedDeps
    halfcircle(d.t1, d.t2, d.y, d.color, d.height) #20 should be half the token height

getTokenPositions = (sentenceId) ->
  positions = []
  tableOffset = jq('table').position().left
  if debugging then jq('#holder').first().prepend("<br>tableOffset: " + tableOffset)
  jq('#sentence'+numSentences)
    .find(".token")
    .each( ->
      positions = positions.concat([
        left:  $(this).position().left + 2 - tableOffset # +2 to account for the width of the line
        width: $(this).width()
      ]))
  for p in positions
    p.center = (p.width / 2) + p.left
  positions

handleMsg = (data) ->
  #print "handling message"
  jsonStr = JSON.stringify(JSON.parse(data))
  what = JSON.parse(data)
  tokenRow = '<tr><td><span class="token">' + (what.tokens.join ' </span></td> <td><span class="token">') + '</span></td></tr>'
  posRow = '<tr><td><span class="pos">' + (what.pos.join ' </span></td> <td><span class="pos">') + '</span></td></tr>'
  jq('ul:first').prepend('<li><div id="sentence'+numSentences+'"><br><table cellspacing="5">' + tokenRow + posRow + '</table></div></li>')
  tokPositions = getTokenPositions(numSentences)
  calculatedDeps = calcDeps(what.deps, tokPositions)
  sentWidth = Math.max.apply(null, tokPositions.map((p) -> p.left + p.width))
  sentHeight = maxDepHeight(calculatedDeps)
  if debugging
    jq('#holder').first().prepend("width: " + JSON.stringify(sentWidth))
    jq('#holder').first().prepend("height: " + JSON.stringify(sentHeight))
    jq('#holder').first().prepend("calculated deps: " + JSON.stringify(calculatedDeps))
    jq('#holder').first().prepend("token positions: " + JSON.stringify(tokPositions))
    jq('#holder').first().prepend("<br>deps: " + JSON.stringify(what.deps))
  makeDepBox(sentWidth, sentHeight)
  drawDeps(calculatedDeps)
  jq("#sentence"+numSentences).attr("align", "center")
  numSentences = numSentences + 1

#################
# Request stuff #
#################

host = "http://localhost:8888"

debug = (msg) -> jq("#debug").html(msg)

send = (msg) ->
  client = new XMLHttpRequest()
  client.open("GET", host + '/sample', false)
  client.setRequestHeader("Content-Type", "text/plain")
  client.send('')
  if (client.status == 200)
    handleMsg(client.responseText)
  else
    alert('Non 200 server response.')

jq("#message").bind(
  'keydown'
  ->
    jq("#submit").removeAttr "disabled"
    jq(this).unbind 'keyDown'
)

jq("#frm").submit (e) ->
  e.preventDefault()
  send this.message.value
  this.message.value = ''
  false

jq("#tooglr").click (e) ->
  e.preventDefault()
  toggleConnection()
  false
