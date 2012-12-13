# LUPOSDATE Semantic Web Database Management System

## Module internetofevents

This module contains the basic components of a publish-/subscribe system for the internet of events.
In the internet of events producers generate events (e.g., the current time, information about a current auction, weather information, ...), which send their events to a broker.
The broker checks, which consumers consume received events and triggers them.
A consumer specifies the events it is interested in by specifying a stream-based SPARQL query (for which we have also a graphical user interface to easily create the stream-based query).
A consumer also specifies the action which should be done after receiving events.

The following producers are currently implemented:
- event after a button is clicked
- event containing a counter
- empty event
- BitCoin exchange rates
- system monitor events (cpu usage, uptime)
- blue tooth events
- twitter search events
- eBay auction events

The following actions are currently implemented:
- message box action
- play audio file
- send email

To use the publish-/subscribe system, you have to start
- the broker (lupos.event.Main_Broker)
- a consumer (lupos.event.gui.Main)
- and at least one of the producers (in package lupos.event.producers)