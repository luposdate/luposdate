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
- system monitor events (cpu usage, uptime, cpu frequency, number of cpus, os, total and free physical/swap bytes)
- blue tooth events
- twitter search events
- eBay auction events
- DB train delays
- Mensa meals events
- news feeds with annotated data from DBPedia
- alarm clock service
- Moon and sunrise events
- weather information (e.g., at Lübeck or in Berlin)
- finance stock events
- Lotto events
- water temperature events
- wind (for sealing) events
- General producer for events on web pages
- General producer for events based on YQL queries

The following actions are currently implemented:
- sliding window
- message box action
- play audio file
- send email

To use the publish-/subscribe system, you have to start
- the broker (lupos.event.broker.centralized.Broker for a centralized broker, or lupos.event.broker.distributed.MasterBroker and several lupos.event.broker.distributed.SubBroker for distributed brokers)
- a consumer (lupos.event.consumer.app.Main or lupos.event.consumer.html.Main for generation of html content)
- and at least one of the producers (in package lupos.event.producer)