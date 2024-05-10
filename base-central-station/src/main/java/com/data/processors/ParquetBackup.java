package com.data.processors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ParquetBackup {
    private static int NOStations;
    private static Schema schema;
    private static final int BATCH_SIZE = 10 ;//* 1024;
    private BlockingQueue<GenericData.Record>[] buffers;
    private ParquetWriter<GenericData.Record>[] writers;

    public ParquetBackup(String path, int stations) throws IOException {
        NOStations = stations;
        schema = getSchema();

        writers = new ParquetWriter[NOStations];
        for (int i = 0; i < NOStations; i++) {
            writers[i] = createWriter(schema, path + "Station_" + (i + 1) + ".parquet");
        }

        buffers = new BlockingQueue[NOStations];
        for (int i = 0; i < NOStations; i++) {
            buffers[i] = new LinkedBlockingQueue<>();
        }
    }

    private static ParquetWriter<GenericData.Record> createWriter(Schema schema, String output) throws IOException {
        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                .<GenericData.Record>builder(new Path(output))
                .withSchema(schema)
                .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build())
        {
            return writer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Schema getSchema() {
        String schemaJson = """
                {
                  "type": "record",
                  "name": "WeatherRecord",
                  "fields": [
                    {"name": "station_id", "type": "long"},
                    {"name": "s_no", "type": "long"},
                    {"name": "battery_status", "type": "string"},
                    {"name": "status_timestamp", "type": "long"},
                    {
                      "name": "weather",
                      "type": {
                        "type": "record",
                        "name": "WeatherInfo",
                        "fields": [
                          {"name": "humidity", "type": "int"},
                          {"name": "temperature", "type": "int"},
                          {"name": "wind_speed", "type": "int"}
                        ]
                      }
                    }
                  ]
                }""";
        return new Schema.Parser().parse(schemaJson);
    }

    public void archiveRecord(JSONObject json) throws IOException {
        int station_id = json.getInt("station_id");
        GenericData.Record record = convertToAvro(json);
        buffers[station_id - 1].add(record);
        if (buffers[station_id - 1].size() >= BATCH_SIZE) {
            writeBatch(writers[station_id - 1], buffers[station_id - 1]);
        }
    }

    private GenericData.Record convertToAvro(JSONObject json) throws IOException {
        try {
            GenericData.Record record = new org.apache.avro.generic.GenericData.Record(schema);
            record.put("station_id", json.getLong("station_id"));
            record.put("s_no", json.getLong("s_no"));
            record.put("battery_status", json.getString("battery_status"));
            record.put("status_timestamp", json.getLong("status_timestamp"));

            JSONObject weather = json.getJSONObject("weather");
            GenericData.Record weatherRecord = new org.apache.avro.generic.GenericData.Record(schema.getField("weather").schema());
            weatherRecord.put("humidity", weather.getInt("humidity"));
            weatherRecord.put("temperature", weather.getInt("temperature"));
            weatherRecord.put("wind_speed", weather.getInt("wind_speed"));

            record.put("weather", weatherRecord);
            return record;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeBatch(ParquetWriter<GenericData.Record> writer, BlockingQueue<GenericData.Record> batch) throws IOException {
        try {
            GenericData.Record record;
            while ((record = batch.poll(100, TimeUnit.MILLISECONDS)) != null) {
                writer.write(record);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
