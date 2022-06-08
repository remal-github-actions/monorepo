package name.remal.github_actions.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.Instantiatable;
import java.io.IOException;
import java.io.Serializable;

class JsonPrettyPrinter
    implements PrettyPrinter, Instantiatable<JsonPrettyPrinter>, Serializable {

    @Override
    public JsonPrettyPrinter createInstance() {
        return new JsonPrettyPrinter();
    }


    private static final String INDENT = "  ";

    private transient int depth = 0;

    private void writeIndent(JsonGenerator gen) throws IOException {
        for (int i = 0; i < depth; ++i) {
            gen.writeRaw(INDENT);
        }
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw("\n");
    }

    @Override
    public void writeStartObject(JsonGenerator gen) throws IOException {
        gen.writeRaw("{");
        ++depth;
    }

    @Override
    public void beforeObjectEntries(JsonGenerator gen) throws IOException {
        gen.writeRaw("\n");
        writeIndent(gen);
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(": ");
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(",\n");
        writeIndent(gen);
    }

    @Override
    public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {
        --depth;
        if (1 <= nrOfEntries) {
            gen.writeRaw("\n");
            writeIndent(gen);
        }
        gen.writeRaw("}");
    }

    @Override
    public void writeStartArray(JsonGenerator gen) throws IOException {
        gen.writeRaw("[");
        ++depth;
    }

    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException {
        gen.writeRaw("\n");
        writeIndent(gen);
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
        gen.writeRaw(",\n");
        writeIndent(gen);
    }

    @Override
    public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
        --depth;
        if (1 <= nrOfValues) {
            gen.writeRaw("\n");
            writeIndent(gen);
        }
        gen.writeRaw("]");
    }

}
