<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>%s</title>
<script type="text/javascript">
function windowTitle() {
  if(location.href.indexOf('is-external=true') === -1) {
    parent.document.title = "%s";
  }
}
</script>
</head>
<body onload="windowTitle();">
%s
%s
<hr>
<h1>%s</h1>
%s
<hr>
%s
</body>
</html>