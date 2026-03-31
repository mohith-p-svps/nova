public class InterpolatedStringNode extends Node {
    String template; // raw template with {expr} placeholders

    InterpolatedStringNode(String template) {
        this.template = template;
    }
}