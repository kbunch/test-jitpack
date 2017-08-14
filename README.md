RxBus - An event bus by Dreamsocket
=============================

RxBus is an event bus that leverages RxJava.

RxBus was designed with an interface very similar to jQuery or Backbone's event API. 
It includes guaranteed event ordering based on time of subscription, ability to prioritize subscribers, 
ability to cancel events, overloaded unsubscribe methods (off) that allow unsubscribing in different ways 
(all events for a bus, all events for an event type, all events for a context, or a specific event for a specific context). 


License
-------

    Copyright 2017 Dreamsocket, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.