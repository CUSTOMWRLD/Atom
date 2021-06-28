package com.customwrld.atom.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.Date;

@AllArgsConstructor
public class RecordValue {
    @Getter @Setter Date date;
    @Getter @Setter String value;

    public RecordValue(Document document) {
        this.date = document.getDate("date");
        this.value = document.getString("value");
    }

    public Document toDocument() {
        var document = new Document();
        document.append("date", this.date);
        document.append("value", this.value);
        return document;
    }
}
