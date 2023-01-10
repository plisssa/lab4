import java.awt.*;
import javax.swing.*;
//интерфейс - класс в кототором методы не реализованы оверрайд - переопределение метода
//implement -реализация интерфейса экстенс - наследование класса


import java.awt.geom.Rectangle2D;
import java.awt.event.*;

//Класс FractalExplorer позволяет исследовать различные части фрактала с помощью
// создания и отображения графического интерфейса Swing и обработки событий,
// вызванных различными взаимодействиями с пользователем.
//Класс для отображения фрактала
public class FractalExplorer {
    /** Целочисленный размер отображения - это ширина и высота отображения в пикселях. **/
    private int displaySize;
    //Константы
    private static final String TITLE = "Фрактал";
    private static final String RESET = "Сброс";
    /**
     * Ссылка JImageDisplay для обновления отображения с помощью различных методов как
     * фрактал вычислен.
     */
    private JImageDisplay display;
    /** Объект FractalGenerator для каждого типа фрактала.
     * Будет использоваться ссылка на базовый класс
     * для отображения других видов фракталов в будущем.
     **/
    private FractalGenerator fractal;
    /**
     * Объект Rectangle2D.Double, который определяет диапазон
     * то, что мы в настоящее время показываем.
     */
    private Rectangle2D.Double range;

    /**Имплементируем интерфейс ActionListener для кнопки сброса
     * Обработка событий от кнопки сброса
     * Обработчик должен сбросить диапазон к начальному,
     * определенному генератором, а затем перерисовать фрактал.
     */
    class ResetButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            fractal.getInitialRange(range);
            drawFractal();
        }
    }
    //Наследуем MouseAdapter для обработки событий мыши
    /**
     * Класс для обработки событий MouseListener с дисплея.
     * Когда обработчик получает событие щелчка мыши, он отображает пиксель-
     * координаты щелчка в области фрактала, который
     * отображается, а затем вызывает функцию RecenterAndZoomRange () генератора
     * метод с координатами, по которым был выполнен щелчок, и масштабом 0,5
     * -происходит увеличение при нажатии
     */
    class MouseHandler extends MouseAdapter {
        @Override //переопределение метода
        public void mouseClicked(MouseEvent e) {
            display.clearImage();
            /** Получаем координату x области отображения щелчка мыши. **/
            int x = e.getX();
            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width, displaySize, x);
            /** Получаем координату y области отображения щелчка мышью. **/
            int y = e.getY();
            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height, displaySize, y);
            /**
             * Вызывааем метод генератора RecenterAndZoomRange() с помощью
             * координаты, по которым был выполнен щелчок, и шкала 0,5.
             */
            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            /**
             * Перерисовываем фрактал после изменения отображаемой области.
             */
            drawFractal();
        }
    }

    //Точка входа в программу
    /**
     * Статический метод main() для запуска FractalExplorer. Инициализирует новый
     * Экземпляр FractalExplorer с размером дисплея 800, вызывает
     * createAndShowGU () в объекте проводника, а затем вызывает
     * drawFractal() в проводнике, чтобы увидеть исходный вид.
     */
    public static void main(String[] args){
        FractalExplorer fractalExplorer = new FractalExplorer(800);
        fractalExplorer.createAndShowGUI();
    }

    //Конструктор класса
    /**
     * Конструктор, который принимает размер отображения, сохраняет его и
     * инициализирует объекты диапазона и фрактал-генератора.
     */
    public FractalExplorer(int displaySize){
        /** Размер дисплея  **/
        this.displaySize = displaySize;
        /** Инициализирует фрактальный генератор и объекты диапазона. **/
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
    }

    //Метод для инициализации графического интерфейса Swing
    /**
     * Этот метод инициализирует графический интерфейс Swing с помощью JFrame, содержащего
     * Объект JImageDisplay и кнопку для очистки дисплея
     */
    public void createAndShowGUI(){
        JFrame frame = new JFrame(TITLE);//рамка с названием
        /** Вызываем операцию закрытия фрейма по умолчанию на "выход".. **/
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        display = new JImageDisplay(displaySize, displaySize);
        /** Установите для frame использование java.awt.BorderLayout для своего содержимого. **/
        /** Добавьте объект отображения изображения в BorderLayout.CENTER position.*/
        frame.add(display, BorderLayout.CENTER);
        /** Создаем кнопку очистки. **/
        JButton resetButton = new JButton(RESET);
        /** Экземпляр ButtonHandler на кнопке сброса. **/
        ResetButtonHandler resetButtonHandler = new ResetButtonHandler();
        resetButton.addActionListener(resetButtonHandler);
        /** Экземпляр MouseHandler в компоненте фрактального отображения. **/
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);
        /** Добавьте объект отображения кнопки в BorderLayout.SOUTH position.*/
        frame.add(resetButton, BorderLayout.SOUTH);
        /**
         * Данные операции правильно разметят содержимое окна
         * Размещаем содержимое фрейма, делаем его видимым и
         * запрещаем изменение размера окна.
         */
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);//изменение размера окна
        drawFractal();
    }

    //Метод для отрисовки фрактала
    /**
     * Приватный вспомогательный метод для отображения фрактала. Этот метод проходит
     * через каждый пиксель на дисплее и вычисляет количество
     * итераций для соответствующих координат во фрактале
     * Область отображения. Если количество итераций равно -1, установит цвет пикселя.
     * в черный. В противном случае выберет значение в зависимости от количества итераций.
     * Обновит дисплей цветом для каждого пикселя и перекрасит
     * JImageDisplay, когда все пиксели нарисованы.
     */
    private void drawFractal(){
        /**Проходим через каждый пиксель на дисплее **/
        for(int i = 0; i < displaySize; i++){
            for(int j = 0; j < displaySize; j++){
                /**
                 * Находим соответствующие координаты xCoord и yCoord
                 * в области отображения фрактала.
                 */
                double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, i);
                double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, j);
                /**
                 * Вычисляем количество итераций для координат в
                 * область отображения фрактала.
                 */
                int iteration = fractal.numIterations(xCoord, yCoord);
                /** If number of iterations is -1, set the pixel to black.
                 * т.е. точка не выходит за границы, установите пиксель в черный цвет (для rgb значение 0)
                 **/
                if (iteration == -1) {
                    display.drawPixel(i, j, 0);
                }
                else {
                    /**
                     * В противном случае выбераем значение оттенка на основе числа
                     * итераций.
                     * цветовое пространство HSV: поскольку значение цвета варьируется от 0 до 1,
                     * получается плавная последовательность цветов от красного к желтому, зеленому, синему,
                     * фиолетовому и затем обратно к красному!
                     */
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                    /** Обновляем дисплей цветом для каждого пикселя. **/
                    display.drawPixel(i, j, rgbColor);
                }
            }
            /**
             * Когда все пиксели будут нарисованы, перекрасим JImageDisplay, чтобы он соответствовал
             * текущее содержимому его изображения
             * вызовем функцию repaint() для компонента
             */
            display.repaint();
        }
    }
}

