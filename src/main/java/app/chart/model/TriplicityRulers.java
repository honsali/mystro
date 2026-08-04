package app.chart.model;

import app.chart.data.Planet;

public record TriplicityRulers(Planet day, Planet night, Planet participating) {
}
