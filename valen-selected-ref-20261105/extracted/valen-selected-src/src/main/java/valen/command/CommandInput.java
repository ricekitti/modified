/*
 * Decompiled with CFR 0.152.
 */
package valen.command;

public final class CommandInput {
    private final String rawMessage;
    private final String label;
    private final String[] arguments;

    public CommandInput(String string, String string2, String[] stringArray) {
        this.rawMessage = string == null ? "" : string;
        this.label = string2 == null ? "" : string2;
        this.arguments = stringArray == null ? new String[]{} : (String[])stringArray.clone();
    }

    public String getRawMessage() {
        return this.rawMessage;
    }

    public String getLabel() {
        return this.label;
    }

    public int argumentCount() {
        return this.arguments.length;
    }

    public String[] getArguments() {
        return (String[])this.arguments.clone();
    }

    public String getArgument(int n) {
        if (n < 0 || n >= this.arguments.length) {
            return null;
        }
        return this.arguments[n];
    }

    public String joinArguments(int n) {
        if (n < 0 || n >= this.arguments.length) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = n; i < this.arguments.length; ++i) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(this.arguments[i]);
        }
        return stringBuilder.toString();
    }
}

