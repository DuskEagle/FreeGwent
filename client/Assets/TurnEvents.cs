using System;
using System.Collections.Generic;

public class TurnEvents {
    public List<TurnEvent> events;

    public TurnEvents(IEnumerable<TurnEvent> events) {
        this.events = new List<TurnEvent>(events);
    }
}
