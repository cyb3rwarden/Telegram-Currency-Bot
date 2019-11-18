package by.mksn.gae.easycurrbot.input

import by.mksn.gae.easycurrbot.AppConfig
import by.mksn.gae.easycurrbot.exchange.ExchangeRateService
import by.mksn.gae.easycurrbot.grammar.expression.ExpressionType
import by.mksn.gae.easycurrbot.util.Result
import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test


class InputQueryParserTest {
    companion object {
        private lateinit var config: AppConfig

        private lateinit var queryParser: InputQueryParser

        private lateinit var exchanger: ExchangeRateService

        @BeforeClass @JvmStatic
        fun setUp() {
            val httpClient = HttpClient(Apache) {
                install(JsonFeature) {
                    serializer = GsonSerializer {
                        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    }
                }
            }
            config = AppConfig.create("application.conf")
            exchanger = ExchangeRateService(httpClient, config)
            queryParser = InputQueryParser(config, exchanger)
        }
    }

    @Test
    fun `parse normal query 1`() {
        val input = "12012.12"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("12012.12"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("12012.12000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query 2`() {
        val input = "12012.12BYN"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("12012.12"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("12012.12000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with metric prefix 1`() {
        val input = "10k"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("10000"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("10000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with metric prefix 2`() {
        val input = "1m"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("1000000"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1000000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with metric prefix 3`() {
        val input = "1kkBYN"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("1000000"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1000000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with metric prefix 4`() {
        val input = "1 M BYN"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("1000000"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1000000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with metric prefix 5`() {
        val input = "1kkc"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("CZK")))
        assertThat(res.baseCurrency, `is`("CZK"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expression, `is`("1000"))
        assertThat(res.expressionResult, `is`("1000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse normal query with metric prefix 6`() {
        val input = "1кр"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("RUB")))
        assertThat(res.baseCurrency, `is`("RUB"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expression, `is`("1000"))
        assertThat(res.expressionResult, `is`("1000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }


    @Test
    fun `parse normal query with metric prefix 7`() {
        val input = "1kkc + 10k$"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("CZK", "USD")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.type, `is`(ExpressionType.MULTI_CURRENCY_EXPR))
        assertThat(res.expression, `is`("1000 CZK + 10000 USD"))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(1000.toBigDecimal(), "CZK") +
                exchanger.exchangeToApiBase(10000.toBigDecimal(), "USD")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }


    @Test
    fun `parse normal query with metric prefix 8`() {
        val input = "1k BYN + 10k BYN"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expression, `is`("1000 + 10000"))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(11000.toBigDecimal(), "BYN")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse normal query with whitespace in number 1`() {
        val input = "100 000 ,h"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("100000"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("100000.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }


    @Test
    fun `parse normal query with whitespace in number 2`() {
        val input = "100k 000 ,h"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse currency pattern query 1`() {
        val input = "12012.12 ,h"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("12012.12"))
        assertThat(res.expressionResult, `is`("12012.12000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }


    @Test
    fun `parse currency pattern query 2`() {
        val input = "12 he,kz"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("RUB")))
        assertThat(res.baseCurrency, `is`("RUB"))
        assertThat(res.expression, `is`("12"))
        assertThat(res.expressionResult, `is`("12.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse expression query 1`() {
        val input = "3-(2*3)+1"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("3 - 2*3 + 1"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("-2.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse expression query 2`() {
        val input = "0,1+0,2"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("0.1 + 0.2"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("0.30000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse expression query 3`() {
        val input = "0,1 + 0,2"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.expression, `is`("0.1 + 0.2"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("0.30000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse expression query 4`() {
        val input = "(0,1 + 0,2) / (2 * 2) !CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("(0.1 + 0.2)/(2*2)"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("0.07500000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse expression query 5`() {
        val input = "2+7--7 +CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("2 + 7 - (-7)"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(16.toBigDecimal(), "BYN")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse expression query 6`() {
        val input = "-7+2+7 !CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.expression, `is`("-7 + 2 + 7"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(2.toBigDecimal(), "BYN")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse expression query 7`() {
        val input = "2+7* - 2 !CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.expression, `is`("2 + 7*(-2)"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase((-12).toBigDecimal(), "BYN")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }


    @Test
    fun `parse multi currency expression query 1`() {
        val input = "10 USD + 5 USD +CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD")))
        assertThat(res.baseCurrency, `is`("USD"))
        assertThat(res.expression, `is`("10 + 5"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("15.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse multi currency expression query 2`() {
        val input = "(2 + 7 )USD + 2EUR +CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD", "EUR")))
        assertThat(res.expression, `is`("(2 + 7) USD + 2 EUR"))
        assertThat(res.type, `is`(ExpressionType.MULTI_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(
                exchanger.exchangeToApiBase(9.toBigDecimal(), "USD") +
                exchanger.exchangeToApiBase(2.toBigDecimal(), "EUR")
        ))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse multi currency expression query 3`() {
        val input = "(2 + 7 )USD * 2 + 2EUR/2 &CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD", "EUR")))
        assertThat(res.expression, `is`("(2 + 7) USD*2 + 2 EUR/2"))
        assertThat(res.type, `is`(ExpressionType.MULTI_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(
                exchanger.exchangeToApiBase(18.toBigDecimal(), "USD") +
                exchanger.exchangeToApiBase(1.toBigDecimal(), "EUR")
        ))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }


    @Test
    fun `parse multi currency expression query with prefixed currency 1`() {
        val input = "USD(2 + 7 ) * 2 + 2/2EUR &CZK"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD", "EUR")))
        assertThat(res.expression, `is`("(2 + 7)*2 USD + 2/2 EUR"))
        assertThat(res.type, `is`(ExpressionType.MULTI_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`(
                exchanger.exchangeToApiBase(18.toBigDecimal(), "USD") +
                exchanger.exchangeToApiBase(1.toBigDecimal(), "EUR")
        ))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse chained simple operands expression query 1`() {
        val input = "1000$ * 0.91 / 10"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD")))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expression, `is`("1000*0.91/10"))
        assertThat(res.expressionResult, `is`("91.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse chained simple operands expression query 2`() {
        val input = "1000$ * 0.91 / 10 * 10"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD")))
        assertThat(res.baseCurrency, `is`("USD"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expression, `is`("1000*0.91/10*10"))
        assertThat(res.expressionResult, `is`("910.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse chained simple operands expression query 3`() {
        val input = "10 * 10 * 10 eUrO + 10 USD"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("EUR", "USD")))
        assertThat(res.expression, `is`("10*10*10 EUR + 10 USD"))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(1000.toBigDecimal(), "EUR") +
                exchanger.exchangeToApiBase(10.toBigDecimal(), "USD")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse chained simple operands expression query 4`() {
        val input = "(1000$ * 0.91) / 10 * 10"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD")))
        assertThat(res.baseCurrency, `is`("USD"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_CURRENCY_EXPR))
        assertThat(res.expressionResult, `is`("910.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse invalid value query`() {
        val input = "asd?/"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid expression query 1`() {
        val input = "/*123 &BYN"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }
    @Test
    fun `parse invalid expression value query 2`() {
        val input = "%23"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid expression value query 3`() {
        val input = "123^^"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(4))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid expression value query 4`() {
        val input = "123/0"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid expression value query 5`() {
        val input = "*1000"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid expression value query 6`() {
        val input = "1.9к$ * 0ю91"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        println(res.component2()!!.toMarkdown())
        assertThat(res.component2()!!.errorPosition, `is`(10))
    }

    @Test
    fun `parse invalid multi currency expression query 1`() {
        val input = "123EUR / 123 USD"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(14))
        println(res.component2()!!.toMarkdown())
    }


    @Test
    fun `parse invalid multi currency expression query 2`() {
        val input = "123EUR / 0 + 23 USD"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid multi currency expression query 3`() {
        val input = "123 && EUR / 0 + 23 USD"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(5))
        println(res.component2()!!.toMarkdown())
    }


    @Test
    fun `parse invalid multi currency expression query 4`() {
        val input = "123 EUR EUR"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(9))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid multi currency expression query 5`() {
        val input = "10 долларов + 10 евро / 0"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid multi currency expression query 6`() {
        val input = "10 / 0 +долларов"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse invalid multi currency expression query 7`() {
        val input = "10 долларов + 10 asdf"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(18))
        println(res.component2()!!.toMarkdown())
    }

    @Test
    fun `parse query with other base 1`() {
        val input = "18$"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("USD")))
        assertThat(res.expression, `is`("18"))
        assertThat(res.baseCurrency, `is`("USD"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("18.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB")))
    }

    @Test
    fun `parse query with other base 2`() {
        val input = "1 UAH"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("UAH")))
        assertThat(res.expression, `is`("1"))
        assertThat(res.baseCurrency, `is`("UAH"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "UAH")))
    }

    @Test
    fun `parse query with additions`() {
        val input = "18 +кроны !br"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("BYN")))
        assertThat(res.baseCurrency, `is`("BYN"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`(exchanger.exchangeToApiBase(18.toBigDecimal(), "BYN")))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "CZK")))
    }

    @Test
    fun `parse query non-default base`() {
        val input = "18грн !aeyns"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("UAH")))
        assertThat(res.baseCurrency, `is`("UAH"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("18.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "UAH", "GBP")))
    }

    @Test
    fun `parse currency-only query 1`() {
        val input = "Гривна"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("UAH")))
        assertThat(res.baseCurrency, `is`("UAH"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "UAH")))
    }

    @Test
    fun `parse currency-only query 2`() {
        val input = "рубль &гривна"
        val res = queryParser.parse(input).get()
        assertThat(res.involvedCurrencies, `is`(listOf("RUB")))
        assertThat(res.baseCurrency, `is`("RUB"))
        assertThat(res.type, `is`(ExpressionType.SINGLE_VALUE))
        assertThat(res.expressionResult, `is`("1.00000000".toBigDecimal()))
        assertThat(res.targets, `is`(listOf("BYN", "USD", "EUR", "RUB", "UAH")))
    }


    @Test
    fun `parse invalid currency-only query 1`() {
        val input = "доллар ?"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(8))
        println(res.component2()!!.toMarkdown())
    }



    @Test
    fun `parse invalid currency-only query 2`() {
        val input = "asdf"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Failure<InputError>)
        assertThat(res.component2()!!.errorPosition, `is`(1))
        println(res.component2()!!.toMarkdown())
    }


    @Test
    fun `parse very long number query`() {
        val input = "usd 488328938372887977341537259497997851352671159292899697236058208809454048246899111241332161343881238402187713643910538138490086922551030374059966793632190643617540775466354136146108018361168082820587948041800957124719210860435589413028616075788651235472"
        val res = queryParser.parse(input)
        assertTrue(res is Result.Success<InputQuery>)
    }

}