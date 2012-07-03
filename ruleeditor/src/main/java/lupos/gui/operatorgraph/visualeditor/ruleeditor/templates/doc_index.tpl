<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<head>
<title>%s</title>
<script type="text/javascript">
  targetPage = window.location.search;

  if(targetPage !== "" && targetPage !== "undefined") {
    targetPage = targetPage.substring(1);
  }

  if(targetPage.indexOf(":") !== -1) {
    targetPage = "undefined";
  }

  function loadFrames() {
    if(targetPage !== "" && targetPage !== "undefined") {
      top.rulesFrame.location = top.targetPage;
    }
  }
</script>
</head>
<frameset cols="20&#37;,80&#37;" onLoad="top.loadFrames()">
<frameset rows="30&#37;,70&#37;">
<frame src="packagesOverviewFrame.html" name="packagesOverviewFrame">
<frame src="allrulesOverviewFrame.html" name="rulesOverviewFrame">
</frameset>
<frame src="packagesDescriptionFrame.html" name="rulesFrame">
<noframes>
<h2>Frame Alert</h2>
This document is designed to be viewed using the frames feature.
If you see this message, you are using a non-frame-capable web client.<br>
Link to <a href="packagesDescriptionFrame.html">Non-frame version.</a>
</noframes>
</frameset>
</html>