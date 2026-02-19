# FoodScan Data Aggregator

A reactive Spring Boot service that acts as the central **product database and enrichment layer** for the FoodScan platform. It stores, retrieves, searches, and enriches food product data — including nutrition, additives, categories, and AI opinions — and exposes it via a WebFlux REST API.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.2, Spring WebFlux (reactive) |
| Database | PostgreSQL (R2DBC reactive driver) |
| Migrations | Flyway 10 |
| Barcode Scanning | Google ZXing |
| Object Storage | AWS S3 SDK v2 |
| Messaging | Spring Kafka |
| Monitoring | Micrometer + Prometheus |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Running

| Property | Default |
|---|---|
| `APP_SERVER_PORT` | `8082` |
| Management port | `8092` |
| Base path | `/api/aggregate` |
| Swagger UI | `http://localhost:8082/api/aggregate/swagger/swagger-ui.html` |
| OpenAPI spec | `http://localhost:8082/api/aggregate/swagger/api-docs` |

Database connection defaults point to `food-scan.main:5432/postgres` schema `aggregate`. Override with `database.*` properties or standard Spring R2DBC/Flyway properties.

---

## Project Structure

```
src/main/java/ai/foodscan/aggregate/db/
├── AggregateDbApplication.java          # Entry point
├── config/
│   ├── OpenFoodClientConfig.java        # WebClient for OpenFood external API
│   └── ClientLoggingConfig.java         # HTTP client logging with field masking
├── router/
│   ├── ProductRouter.java               # Product endpoint routes
│   ├── CategoryRouter.java              # Category endpoint routes
│   └── AdditiveRouter.java              # Additive endpoint routes
├── handler/
│   ├── ProductHandler.java              # Product request handling
│   ├── CategoryHandler.java             # Category request handling
│   ├── AdditiveHandler.java             # Additive request handling
│   └── WebErrorHandler.java             # Global error → HTTP status mapping
├── service/
│   ├── ProductService.java              # Product CRUD + enrichment pipeline
│   ├── CategoryService.java             # Category hierarchy + product-by-category
│   ├── AdditiveService.java             # Additive lookup & enrichment
│   ├── SearchProductService.java        # Full-text & barcode search
│   ├── BarcodeService.java              # ZXing barcode image decoding
│   ├── EanCheckService.java             # EAN-13 validation
│   ├── ProductFetchCountService.java    # Search/fetch statistics tracking
│   └── CalorieBurnService.java          # Calorie burn time estimates
├── calculator/
│   └── NutritionCalculator.java         # Nutrition comparison vs category avg
├── enhancer/
│   └── ProductFormatter.java            # Image URL generation
├── mapper/
│   ├── ProductMapper.java               # ProductEntity ↔ Product (full)
│   ├── MinimalProductMapper.java        # ProductEntity → MinimalProduct
│   └── ProductEntityMapper.java         # Merge/update existing entity
├── model/
│   ├── api/                             # API-facing DTOs
│   │   ├── Product.java
│   │   ├── MinimalProduct.java
│   │   ├── Additive.java
│   │   ├── AdditiveRecord.java
│   │   ├── NutritionPer100g.java
│   │   ├── NutritionComparison.java
│   │   ├── StorageConditions.java
│   │   ├── CalorieBurnEstimate.java
│   │   ├── CategoryDto.java
│   │   ├── ProductsByCategoryResponse.java
│   │   ├── Language.java                # Enum: LT, EN
│   │   └── ActivityType.java            # Enum: WALKING, RUNNING, BICYCLING
│   └── db/entity/                       # Database entities
│       ├── ProductEntity.java
│       ├── CategoryEntity.java
│       ├── AdditiveEntity.java
│       └── ProductFetchCountEntity.java
├── repository/
│   ├── ProductRepository.java           # Product queries (search, category, nutrition avg)
│   ├── CategoryRepository.java          # Category hierarchy queries
│   ├── AdditivesRepository.java         # Additive CRUD
│   └── ProductFetchCountRepository.java # Fetch stats queries
├── client/
│   └── OpenFoodClient.java              # External OpenFood Facts API client
├── filter/
│   ├── ClientLoggingFilterFunction.java
│   ├── ClientRequestLoggingInterceptor.java
│   ├── ClientResponseLoggingInterceptor.java
│   └── HeadersAndBodyMasker.java
├── exception/
│   ├── NoProductFoundException.java
│   ├── BarcodeDecodingException.java
│   ├── MissingParameterException.java
│   └── InvalidValueException.java
└── util/
    └── PathVariableExtractor.java

src/main/resources/
├── application.properties
└── db.migration/
    ├── V1__Initial_database_structure.sql
    ├── V2__additives.sql
    ├── V3__category_slugs.sql
    ├── V3.1__update_slugs.sql
    ├── V4__counter_table.sql
    ├── V5__vector_search.sql
    ├── V6__manufacturer_column.sql
    ├── V7__rename_column.sql
    ├── V7.1__manufacturer_name.sql
    └── V8__recommended.sql
```

---

## Database Schema (`aggregate`)

### `products`

The central table. Stores all product data in both English and Lithuanian.

| Column | Type | Notes |
|---|---|---|
| `internal_product_id` | UUID PK | Auto-generated |
| `barcode` | TEXT UNIQUE | EAN-13 validated |
| `name_en`, `name_lt` | TEXT | Product name |
| `image_url` | TEXT | Raw image URL |
| `price` | NUMERIC[] | **Price history** (array, latest = last element) |
| `original_price` | NUMERIC[] | Original price history |
| `raw_description` | TEXT | Unprocessed source description |
| `description_en`, `description_lt` | TEXT | Cleaned descriptions |
| `country_of_origin_en`, `country_of_origin_lt` | TEXT | |
| `net_weight_g` | INTEGER | |
| `ingredients_en`, `ingredients_lt` | TEXT | |
| `additives_en`, `additives_lt` | JSONB | Array of `{shortcode, name, aiAnalysis, isPreservative}` |
| `nutrition_per_100g` | JSONB | `{energyValueKj, fats, saturatedFats, carbohydrates, sugars, proteins, salt, fiber, fattyAcids}` |
| `storage_conditions` | JSONB | `{minTemperature, maxTemperature, postOpeningEn, postOpeningLt}` |
| `packaging_en`, `packaging_lt` | TEXT | |
| `brand` | TEXT | |
| `manufacturer_name` | TEXT | |
| `manufacturer_description_en`, `manufacturer_description_lt` | TEXT | |
| `ai_opinion_en`, `ai_opinion_lt` | TEXT | AI-generated health opinion |
| `ai_datasource` | TEXT | Which AI generated the data |
| `main_category_en/lt` | TEXT | Top-level category |
| `sub_category_en/lt` | TEXT | Second-level category |
| `sub_sub_category_en/lt` | TEXT | Third-level category |
| `original_url_en`, `original_url_lt` | TEXT | Source URLs |
| `allergens_en`, `allergens_lt` | TEXT[] | Allergen arrays |
| `recommended` | BOOLEAN | AI recommendation flag |
| `created_at` | TIMESTAMP | Auto-set |
| `updated_at` | TIMESTAMP | Auto-updated via trigger |

**Indexes:** GIN on all JSONB columns, B-tree on category columns, full-text (accent-insensitive) on name columns.

### `product_fetch_counts`

Tracks how often each product is viewed/searched.

| Column | Type |
|---|---|
| `internal_product_id` | UUID PK |
| `fetch_count` | BIGINT |
| `last_fetched_at` | TIMESTAMPTZ |

### `category_slugs`

URL-friendly category hierarchy for navigation.

| Column | Type |
|---|---|
| `main_category_slug_en/lt` | TEXT |
| `sub_category_slug_en/lt` | TEXT |
| `sub_sub_category_slug_en/lt` | TEXT |
| `main_category_en/lt` | TEXT (display name) |
| `sub_category_en/lt` | TEXT (display name) |
| `sub_sub_category_en/lt` | TEXT (display name) |

**PK:** Composite (`main_category_slug_en`, `sub_category_slug_en`, `sub_sub_category_slug_en`)

### `additives`

Reference table for E-numbers.

| Column | Type |
|---|---|
| `code` | TEXT PK (e.g. "E100") |
| `name_en`, `name_lt` | TEXT |
| `url` | TEXT |
| `is_dangerous` | BOOLEAN |
| `general_usage_in_food_industry_en/lt` | TEXT |
| `usage_in_foods_en/lt` | TEXT |
| `other_information_en/lt` | TEXT |
| `damage_en/lt` | TEXT |

---

## API Endpoints

All paths are prefixed with `/api/aggregate`.

### Products

| Method | Path | Description |
|---|---|---|
| GET | `/v1/products/internal/{id}` | Get product by internal UUID |
| GET | `/v1/products/barcode/{barcode}` | Get product by barcode. Query: `main` (bool), `lang` |
| GET | `/v1/products/search` | Search by `barcode`, `name_en`, `name_lt`. Query: `limit` (max 5) |
| GET | `/v1/products/barcodes` | List all unique barcodes |
| POST | `/v1/products/internal/` | Create product. Query: `id` (UUID) to update existing |
| POST | `/v1/products/barcode-scan` | Upload image (multipart `image`), decode barcode, return product |
| GET | `/v1/products/by-category` | Paginated products. Query: `lang`, `mainCategory`, `subCategory`, `subSubCategory`, `page`, `size` |
| GET | `/v1/products/top-searched` | Most fetched products. Query: `page`, `size` |
| GET | `/v1/products/recent-searched` | Recently fetched products. Query: `page`, `size` |

### Categories

| Method | Path | Description |
|---|---|---|
| GET | `/v1/categories` | Full category tree |
| GET | `/v1/categories/main` | Main categories only |
| GET | `/v1/categories/sub` | Sub-categories. Query: `mainCategorySlug` |
| GET | `/v1/categories/subsub` | Sub-sub-categories. Query: `mainCategorySlug`, `subCategorySlug` |

### Additives

| Method | Path | Description |
|---|---|---|
| GET | `/v1/additives/{code}` | Get additive details by E-code |

### Actuator (port 8092)

| Method | Path | Description |
|---|---|---|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/prometheus` | Prometheus metrics |

---

## Product Enrichment Pipeline

When a product is retrieved, it goes through an automatic enrichment chain:

```
Database Entity
  │
  ├─ 1. ProductFormatter       → Generate image URL from product ID
  ├─ 2. NutritionCalculator    → Compare nutrition vs category average
  ├─ 3. FetchCountService      → Increment view counter
  ├─ 4. CalorieBurnService     → Estimate burn times (walking/running/cycling)
  ├─ 5. AdditiveService        → Map additive shortcodes to full names
  ├─ 6. CategoryService        → Find similar products in same category
  └─ 7. CategoryService        → Find recommended products (flagged)
  │
  ▼
Enriched API Response
```

### Nutrition Comparison

Products are compared against the **average nutritional values of their category** (main + sub + sub-sub). The response includes both the averages and the percentage difference for each nutrient (fats, sugars, salt, energy, etc.).

### Calorie Burn Estimates

Based on `energyValueKj`, calculates minutes needed to burn the calories via:
- Walking (4 kcal/min)
- Running (10 kcal/min)
- Bicycling (8 kcal/min)

---

## Category System

Products are organized in a **3-level hierarchy**:

```
Main Category (e.g. "Dairy Products" / "Pieno produktai")
  └─ Sub Category (e.g. "Milk" / "Pienas")
      └─ Sub-Sub Category (e.g. "Whole Milk" / "Pilno riebumo pienas")
```

Each level has:
- **Display name** in EN and LT
- **URL-friendly slug** in EN and LT

The `category_slugs` table maintains the canonical hierarchy. Products reference categories by display name in their `main_category_en/lt`, `sub_category_en/lt`, `sub_sub_category_en/lt` columns.

---

## External Integrations

### OpenFood Facts API

When a product is requested by barcode and **not found locally**, the service falls back to an external OpenFood service at:

```
GET {openfood.http-schema}://{openfood.host}:{openfood.port}/api/openfood/v1/openfood/translate/{barcode}
```

The response is a `Product` object that gets saved locally for future requests.

### AWS S3

The S3 SDK dependency is present for image storage, though the primary image serving uses a static URL pattern: `https://foodscan.ai/images/{product_id}.png`

### Kafka

Spring Kafka is configured as a dependency for event-driven messaging (consumer/producer), though the current codebase primarily uses it as infrastructure for integration flows.

---

## Search Capabilities

### Full-Text Search
- Uses PostgreSQL `to_tsvector` / `to_tsquery` on product names
- Accent-insensitive via custom `immutable_unaccent` function
- Searches both EN and LT name fields simultaneously

### Barcode Search
- Exact match or partial (`LIKE`) match
- EAN-13 checksum validation before save

### Barcode Image Scanning
- Accepts image upload (multipart)
- Decodes using ZXing multi-format reader
- Failed images saved to disk for debugging

---

## Error Handling

| Exception | HTTP Status |
|---|---|
| `InvalidValueException` | 400 |
| `MissingParameterException` | 400 |
| `IllegalArgumentException` | 400 |
| `ServerWebInputException` | 400 |
| `BarcodeDecodingException` | 400 |
| `NoProductFoundException` | 404 |
| Everything else | 500 |

---

## Data Flow: How a Product Gets Saved

```
1. POST /v1/products/internal/?id=<optional-uuid>
2. Deserialize JSON body → Product DTO
3. Validate EAN-13 barcode checksum
4. If id param provided → load existing entity, merge fields (ProductEntityMapper)
   If no id → check if barcode exists → merge or create new
5. Prices are appended to the price[] history array (not overwritten)
6. Complex fields (additives, nutrition, storage) serialized to JSONB
7. Entity saved via R2DBC
8. Enrichment pipeline runs on the saved product
9. Enriched Product returned in response
```

---

## Key Design Decisions

1. **Reactive end-to-end**: R2DBC + WebFlux means no thread blocking from DB to HTTP response
2. **Price as array**: Historical price tracking without a separate table — latest price is always the last element
3. **Bilingual by design**: Every text field has `_en` and `_lt` variants; language parameter controls which additives/categories are resolved
4. **Enrichment on read**: Nutrition comparison, burn estimates, similar products are computed at query time, not stored
5. **OpenFood fallback**: Local-first with external API as backup for unknown barcodes
6. **Slug-based categories**: URL-friendly navigation separate from display names
7. **Functional routing**: Uses `RouterFunction` instead of `@RestController` annotations

---

## For AI Core Integration

This service is designed to be the **data backbone**. Key integration points for an AI core:

| Concern | How to integrate |
|---|---|
| **Read products** | `GET /v1/products/barcode/{barcode}` or `GET /v1/products/internal/{id}` |
| **Save AI-enriched products** | `POST /v1/products/internal/?id={uuid}` — fields like `ai_opinion_en/lt`, `ai_datasource`, `additives` with `aiAnalysis`, `recommended` flag |
| **Batch discovery** | `GET /v1/products/barcodes` to get all known barcodes |
| **Category browsing** | `/v1/categories/*` endpoints for structured navigation |
| **Search** | `GET /v1/products/search` for lookup before processing |
| **Nutrition data** | `nutrition_per_100g` JSONB field on every product |
| **Additive reference** | `GET /v1/additives/{code}` for E-number details |
| **Statistics** | `top-searched` and `recent-searched` for prioritizing AI processing |

The `ai_opinion_en/lt` and `ai_datasource` fields are already in the schema, ready for an AI core to write analysis results back. The `recommended` boolean flag lets AI mark products as recommended.
