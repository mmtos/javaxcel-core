package com.github.javaxcel.out;

import com.github.javaxcel.annotation.ExcelColumn;
import com.github.javaxcel.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.annotation.ExcelIgnore;
import com.github.javaxcel.annotation.ExcelModel;
import com.github.javaxcel.constant.TargetedFieldPolicy;
import com.github.javaxcel.constant.ToyType;
import com.github.javaxcel.model.*;
import com.github.javaxcel.model.factory.MockFactory;
import com.github.javaxcel.util.ExcelUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelWriterTest {

    @ParameterizedTest
    @ValueSource(classes = {Product.class, ToyBox.class, Toy.class, EducationToy.class})
    public void getFields(Class<?> type) throws IllegalAccessException {
        // given
        ExcelModel annotation = type.getAnnotation(ExcelModel.class);
        Stream<Field> stream = annotation == null || annotation.policy() == TargetedFieldPolicy.OWN_FIELDS
                ? Arrays.stream(type.getDeclaredFields())
                : ExcelUtils.getInheritedFields(type).stream();

        // when
        List<Field> fields = stream
                .filter(field -> field.getAnnotation(ExcelIgnore.class) == null) // Excludes the fields annotated @ExcelIgnore.
//                .filter(field -> TypeClassifier.isWritableClass(field.getType())) // Excludes the fields that are un-writable of excel.
                .collect(Collectors.toList());

        // then
        for (Field field : fields) {
            field.setAccessible(true);

            if (Toy.class.equals(type) || EducationToy.class.equals(type)) {
                System.out.println(field.getName() + ":\t" + field.get(MockFactory.generateRandomBox(1).getAll().get(0)));
            } else if (Product.class.equals(type)) {
                System.out.println(field.getName() + ":\t" + field.get(MockFactory.generateRandomProducts(1).get(0)));
            } else {
                System.out.println(field.getName() + ":\t" + field);
            }
        }
    }

    /**
     * 1. {@link ExcelIgnore}
     * <br>
     * 2. {@link ExcelColumn#value()}
     * <br>
     * 3. {@link ExcelColumn#defaultValue()}
     */
    @Test
    public void writeWithIgnoreAndDefaultValue() throws IOException, IllegalAccessException {
        // given
        File file = new File("/data", "products.xlsx");
        FileOutputStream out = new FileOutputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<Product> products = MockFactory.generateRandomProducts(1000);
        ExcelWriter.init(workbook, Product.class).sheetName("").write(out, products);

        // then
        assertTrue(file.exists());
    }

    /**
     * 1. {@link ExcelModel#policy()}, {@link TargetedFieldPolicy#INCLUDES_INHERITED}
     * <br>
     * 2. {@link ExcelDateTimeFormat#pattern()}
     */
    @Test
    public void writeWithTargetedFieldPolicyAndDateTimePattern() throws IOException, IllegalAccessException {
        // given
        File file = new File("/data", "toys.xlsx");
        FileOutputStream out = new FileOutputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook();

        // when
        List<EducationToy> toys = MockFactory.generateRandomBox(1000).getAll();
        ExcelWriter.init(workbook, EducationToy.class).write(out, toys);

        // then
        assertTrue(file.exists());
    }

    @Test
    public void formatDateTime() {
        // given
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        LocalDateTime dateTime = LocalDateTime.now();

        // when
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yy/M/dd"));
        String formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // then
        System.out.println(formattedDate);
        System.out.println(formattedTime);
        System.out.println(formattedDateTime);
    }

    @Test
    public void stringifyBigNumbers() {
        // given
        String strBigInt = Long.valueOf(Long.MAX_VALUE).toString();
        String strBigDec = "123456789123456789123456789123456789123456.07890";

        // when
        String bigInteger = String.valueOf(BigInteger.valueOf(Long.parseLong(strBigInt)));
        String bigDecimal = String.valueOf(new BigDecimal(strBigDec));

        // then
        assertEquals(bigInteger, strBigInt);
        assertEquals(bigDecimal, strBigDec);
    }

}
