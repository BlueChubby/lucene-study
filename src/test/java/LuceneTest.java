import com.xlm.entity.Sku;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LuceneTest {

    /**
     * StringField 不分词   索引     y 或者 n
     * FloatPoint  分词     索引     n 不存储
     * DoublePoint 分词     索引     n 不存储
     * LongPoint   分词     索引     n 不存储
     * IntPoint    分词     索引     n 不存储
     * StoredField 不分词   不索引    y 存储
     * TextField   分词     索引     y 或者 n
     * NumericDocValuesField        配合其他域排序使⽤
     */

    @Test
    public void createTest1() throws Exception {
        // 1. 采集数据
        List<Sku> skuList = Sku.generateRandomSkus(10);
        // 2. 创建文档对象
        List<Document> documents = new ArrayList<>();
        for (Sku sku : skuList) {
            Document document = new Document();
            //document 文档添加Field域
            //商品 id，不分词，索引，存储
            document.add(new StringField("id", sku.getId(), Field.Store.YES));
            //商品名称，分词， 索引，存储
            document.add(new TextField("name", sku.getName(), Field.Store.YES));
            //商品价格，分词， 索引，不存储
            document.add(new FloatPoint("price", sku.getPrice()));
            //添加价格存储支持
            document.add(new StoredField("price", sku.getPrice()));
            //添加价格支持排序
            document.add(new NumericDocValuesField("price", sku.getPrice()));

            //品牌名称，不分词，索引，存储
            document.add(new StringField("brandName", sku.getBrandName(), Field.Store.YES));
            //分类名称，不分词，索引，存储
            document.add(new StringField("categoryName", sku.getCategoryName(), Field.Store.YES));
            //图片，   不分词，不索引，存储
            document.add(new StoredField("image", sku.getImage()));
            documents.add(document);
        }

        //3. 创建分词器
        Analyzer analyzer = new IKAnalyzer();
        //4. 创建 directory，声明索引库位置
        FSDirectory directory = FSDirectory.open(Paths.get("./indexDir"));
        //5. 写入索引需要的配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //6. 创建IndexWriter 对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //7. 将 document 对象写入索引库
        indexWriter.addDocuments(documents);
        //8. 关闭资源
        indexWriter.close();
    }


    @Test
    public void testIndexSearch() throws Exception {
        Analyzer analyzer = new IKAnalyzer();

        Query query = new QueryParser("brandName", analyzer).parse("苹果");

        FSDirectory directory = FSDirectory.open(Paths.get("./indexDir"));

        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        TopDocs topDocs = indexSearcher.search(query, 10);

        System.out.println("topDocs = " + topDocs.totalHits.value);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            // 获取⽂档
            int docID = scoreDoc.doc;
            Document doc = indexSearcher.doc(docID);
            System.out.println("=============================");

            System.out.println("docID:" + docID);
            System.out.println("id:" + doc.get("id"));
            System.out.println("name:" + doc.get("name"));
            System.out.println("price:" + doc.get("price"));
            System.out.println("brandName:" + doc.get("brandName"));
            System.out.println("image:" + doc.get("image"));
        }

        reader.close();
    }


}
