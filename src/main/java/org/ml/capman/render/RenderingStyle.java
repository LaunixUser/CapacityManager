package org.ml.capman.render;

/**
 * @author Dr. Matthias Laux
 */
public class RenderingStyle {

    private RenderingDirection direction = RenderingDirection.horizontal;
    private RenderingType headerStyle = RenderingType.none;
    private RenderingType dataStyle = RenderingType.none;

    /**
     * @param direction
     * @param headerStyle
     * @param dataStyle
     */
    public RenderingStyle(RenderingDirection direction, RenderingType headerStyle, RenderingType dataStyle) {
        if (direction != null) {
            this.direction = direction;
        }
        if (headerStyle != null) {
            this.headerStyle = headerStyle;
        }
        if (dataStyle != null) {
            this.dataStyle = dataStyle;
        }
    }

    /**
     * @return the direction
     */
    public RenderingDirection getDirection() {
        return direction;
    }

    /**
     * @return the headerStyle
     */
    public RenderingType getHeaderStyle() {
        return headerStyle;
    }

    /**
     * @return the dataStyle
     */
    public RenderingType getDataStyle() {
        return dataStyle;
    }

}
