package com.data.processors.Parquet;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ParquetBackup {
    private static int NOStations;
    private static Schema schema;
    private static final int BATCH_SIZE = 10 ;//* 1024;
    private static String parquetsPath;
    private final BlockingQueue<GenericData.Record>[] buffers;

    public ParquetBackup(String path, int stations) throws IOException {
        NOStations = stations;
        schema = getSchema();
        parquetsPath = path;

        // Create directories in the path named from 1 to 10
        for (int i = 1; i <= NOStations; i++) {
            File file = new File(parquetsPath + "Station_" + i);
            file.mkdirs();
        }

        buffers = new BlockingQueue[NOStations];
        for (int i = 0; i < NOStations; i++) {
            buffers[i] = new LinkedBlockingQueue<>();
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
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm")
                    .format(new java.util.Date (System.currentTimeMillis()));
            String path = parquetsPath + "Station_" + station_id + "/" + "Station_" +
                    station_id + "_" + date + ".parquet";
            System.out.println("Writing batch to " + path + "....");
            writeBatch(buffers[station_id - 1], path);
        }
    }

    private static GenericData.Record convertToAvro(JSONObject json) throws IOException {
        try {
            GenericData.Record record = new org.apache.avro.generic.GenericData.Record(schema);
            record.put("station_id", json.getLong("station_id"));
            record.put("s_no", json.getLong("s_no"));
            record.put("battery_status", json.getString("battery_status"));
            record.put("status_timestamp", json.getLong("status_timestamp"));

            JSONObject weather = json.getJSONObject("weather");
            GenericData.Record weatherRecord = new GenericData.Record(schema.getField("weather").schema());
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

    private static void writeBatch(BlockingQueue<GenericData.Record> batch, String output) throws IOException {
        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                .<GenericData.Record>builder(new Path(output))
                .withSchema(schema)
                .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build()) {
            GenericData.Record record;
            while ((record = batch.poll(100, TimeUnit.MILLISECONDS)) != null) {
                writer.write(record);
            }
            writer.close();
            System.out.println("--------Batch written-------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
