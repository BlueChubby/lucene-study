import com.xlm.entity.Sku;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
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


    @Test
    public void testIndexTest() throws Exception {
        Analyzer analyzer = new IKAnalyzer();
        Directory directory = FSDirectory.open(Paths.get("./indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document document = new Document();
        document.add(new IntPoint("price", 100));
        document.add(new StoredField("price", 100));
        document.add(new StringField("brandName", "华为", Field.Store.YES));
        document.add(new StringField("id", "1", Field.Store.YES));

        indexWriter.updateDocument(new Term("id", "SKU6"), document);
        indexWriter.close();
    }


    @Test
    public void testIndexDelete() throws Exception {
        Analyzer analyzer = new IKAnalyzer();
        Directory directory = FSDirectory.open(Paths.get("./indexDir"));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.deleteDocuments(new Term("id", "1"));
        indexWriter.close();
    }

    @Test
    public void TestWhitespaceAnalyzer() throws Exception {

        // 1. 创建分词器,分析文档,对文档进行分词
        Analyzer analyzer = new WhitespaceAnalyzer();
        // 2. 创建Directory对象,声明索引库的位置
        Directory directory = FSDirectory.open(Paths.get("./indexDir"));
        // 3. 创建IndexWriteConfig对象,写入索引需要的配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 4.创建IndexWriter写入对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        // 5.写入到索引库,通过IndexWriter添加文档对象document
        Document doc = new Document();
        doc.add(new TextField("name", "vivo X23 8GB+128GB 幻夜蓝", Field.Store.YES));
        indexWriter.addDocument(doc);
        // 6.释放资源
        indexWriter.close();
    }


    @Test
    public void TestSimpleAnalyzer() throws Exception {

        // 1. 创建分词器,分析⽂档，对⽂档进⾏分词
        Analyzer analyzer = new SimpleAnalyzer();

        // 2. 创建Directory对象,声明索引库的位置
        Directory directory = FSDirectory.open(Paths.get("./indexDir"));

        // 3. 创建IndexWriteConfig对象，写⼊索引需要的配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // 4.创建IndexWriter写⼊对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // 5.写⼊到索引库，通过IndexWriter添加⽂档对象document
        Document doc = new Document();
        doc.add(new TextField("name", "vivo，X23。 8GB+128GB； 幻夜蓝", Field.Store.YES));
        indexWriter.addDocument(doc);

        // 6.释放资源
        indexWriter.close();
    }

    @Test
    public void TestIKAnalyzer() throws Exception {

        // 1. 创建分词器,分析⽂档，对⽂档进⾏分词
        Analyzer analyzer = new IKAnalyzer();
        // 2. 创建Directory对象,声明索引库的位置
        Directory directory = FSDirectory.open(Paths.get("./indexDir"));
        // 3. 创建IndexWriteConfig对象，写⼊索引需要的配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // 4.创建IndexWriter写⼊对象
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // 5.写⼊到索引库，通过IndexWriter添加⽂档对象document
        Document doc = new Document();
        doc.add(new TextField("name", "vivo X23 8GB+128GB 幻夜蓝,⽔滴屏全⾯屏, 游戏⼿机.移动联通电信全⽹通4G⼿机", Field.Store.YES));
        indexWriter.addDocument(doc);

        // 6.释放资源
        indexWriter.close();

    }

}
