# Any%

Created in Spring 2023 by Mark Mogos.

The inspiration for this language came to me when I was watching a random coding competition video that showed up in my recomended. I saw this and thought, what if the if statement was only an i? how much time would that save? And kept thinking how these people could save maybe even minutes at a a time coding for competitions, and so the idea of Any% was born. Any% is a term used to describe getting to an end goal as fast as possible, no matter how many corners you cut or weird/inefficient ways you go about it. And so, the goal of the language is to get to the end of your coding assignments as fast as possible! :)

A note before you begin reading. Spaces beyond one space do not matter at all and are skipped over, as well as line breaks. Feel free to use them though for organization purposes, and legibility, but if you're trying to go fast why bother!

## Variables

To declare a variable, you write the name of the variable followed by an equals, followed by the content of the variable. It can be done like this:


```x=2``` Declares a variable named x with a value of two. In Any%, the goal is to reduce characters not runtime, so no special declaration command is needed, and neither is an indicator of what type of variable it is. 

```x=3.``` The ```.``` after the number means this variable will be declared as a double.

```x="four"``` Letters with a ```"``` at the beginning and end means it will be declared as a string.

```x=""``` This will create an empty string.

```x=true``` creates a boolean set to true.


When typing a variable, just typing ```x``` or whatever the variable is called is how you call its value.

Also, some variable names are off limits. Just like "if" in java, and letter used in the functions below is off limits (eg: p for print, n for denoting a consistent function, etc).

## Functions

Declaring a function: 

To declare a function, Any% uses a method called d. It works similar to how java works, but a couple differences. It works like this:

```d c I o dbl[i x b d]```

This is an example declaration of a function. Now, I know it might look like jibberish, but the format is as follows:

The first letter denotes whether it is a consistent function or not. If it's not, it can be ommitted. 

The second letter denotes the return type based on its first letter. Since there is no overlap between them, there are no exceptions.

The third and final letter denotes whether a function is open or closed. Open means it can be accessed outside the class, and Closed means it is locked to the specific class.

Whatver comes after that is the name of the function until you add a "[". Then comes the parameters. Parameters are indicated by the type they are exactly how return type uses types, and then the name of the variable (suggested to be one letter, two if required). 

| Consistency                     | Return Type |Open/Closed|Name of Function| Parameter type |Parameter name|
| ------------------------------- | ----------- |-----------|----------------|---|---|
| n - Denotes a consistent fxn    | i - integer |o - Open   |                |Same as return types||
|   - denotes a inconsistent fxn| d - double  |c - Closed |                |||
|   | b - boolean |           |                |||
|   | s - string  |           |                |||
|   | c - char    |           |                |||

The next set of text after the space indicates what actually goes on during the function. 

So in the example above, ```d c I o dbl[i x b d]```, we create a function that is consistent, has an integer return type, is open, has the name "dbl", and an integer "x" and boolean "d" as parameters. 




A function that started with ```n I c``` would be a consistent, integer returning, closed function. Also, the non-consistent function part is intentionally left blank. It would look like: ```d o``` if the function was inconsistent, had a double return, and was open. 


Calling this function is as simple as writing: ```dbl [x y]```

### Short Functions:

## Control Flow

### Loops

Loops are defined, similar to functions, with ```l```. Personally, I reccomend using a capital L to better differentiate lowercase L from capital i, but if going for optimal speed lowercase can be used, or it can be used freely if your font differentiates the two enough. In markdown, it look awfully similar to 1, so I will be using capital L. 

Example loop call: ```L f i=16 i<20 i++ [p "hey"]] ```

Here is the syntax for a loop: 

First letter is just L to signify the start of a loop. 

Second letter signifies what type of loop it is. The options are: w --> while, f --> for

The next section is the parameters of the loop. For a for loop, it goes as follows: 

name of variable, starting value, conditions loop must maintain, and the value by which it changes. So in our original example, the variable is named i, starts at 16, must be less than 20, and increases by 1 each time (only ++ and -- are allowed for variable changes at the moment).

For a while loop, its just a condition, so not much is needed to talk about here. 



### Conditionals

If statements are the primary conditional used, and they work similar to most languages, obviously with some shortenings. Example if:

```i b<10 p B is less than 10.] ```

This function is pretty straightforward and intuitive. It starts with an i, then the condition, then what happens if the condition comes to true. Just like loops, the code inside the if is constituted by what is before a space.

Else statements are actually declared before the if statements conditionals. Here is an example of what that looks like:

```i i i e g<70 g<80 g<90 [p "You got an F"]] [p "You got a C"]] [p "You got a B"]] [p "You got an A!"]]```

This is the classic grade problem that many learn early into coding. The if statements are all declated initially, followed by their conditions. So in this case, an if statement followed by two else ifs and an else lead to the grade problem printing your grade given number g. 

## Operators and Comparators

Operators are very typical for most coding languages. 

```+``` addition. Ex: ```x+y```

```++``` adds one to the variable. Ex: ```x++```

```-``` subtraction. Ex: ```x-y```

```--``` subtracts one from the variable. Ex: ```x--```

```*``` multiplication. Ex: ```x*y```

```/``` divide. Ex: ```x/y```

```%``` modulus. Ex: ```x%y```

```^``` raised to the power of. Ex: ```x^y```


Comparitors:

```>``` Greater than. Ex: returns true if: ```x>y```

```<``` Less than. Ex: returns true if: ```x<y```

```>=``` Greater than or equal to. Ex: returns true if: ```x>=y```

```<=``` Less than or equal to. Ex: returns true if ```x<=y```

```==``` Equal to. Ex: returns true if ```x==y```

```!=``` Not equal to. Ex: returns true if```x!=y```

Other operators:

OR and AND can be recreating using multiplication & addition using unique crosstype operations. 

```!``` Not. Flips whether something is true or false. Ex: ```!(x*y<10))```


[Here](https://docs.google.com/spreadsheets/d/1XJaDg1U-TXVY_RlVO4XcgvTyXEo56T3b6p6BADMNvJk/edit?usp=sharing) is a link to a spreadsheet that shows the interactions of different types of variables through operators.



## Built in Functions: 

### Print: 

```p Have a Nice Day!]```

Above is how the print statement works. It starts with a p, followed by a space then whatever it is you want to say, then ends with a ] to denote the end of the if statement. These spaces do not interfere or affect the end of loops in any way. 

If a space is needed for visual sake, it can be implemented with a ```\```. A ```\``` before a space means the space is ignored, and it moves on to the next 'line'.

I will definately be adding more built in functions as I code and test and find out what I'm typing the most and how I can reduce it, but for now I'm not going to try and guess. 

## Table of All Functions:

|Function|Explanation|
|---|---|
|```d *conditionname(parameters*```| declares a function with a name and parameters. See table in functions section for how to format conditions|
|```'```|Returns the value/expression after the symbol. Ex: 'x returns x in a function|
|```(parameter>expression``` or ```(parameter parameter>expression```|Declares a short function|
|```L f conditions content ```|Declares a for loop with given conditions. See loops section for details on how to format conditions|
|```L w conditions content ```|Declares a while loop with given conditions. See loops section for details on how to format conditions|
|```L wt content```|Declares a while true loop. Runs forever unless a break command is used|
|```i ei ei e conditioni conditionei conditionei condition e contenti contentei contentei contente```| Declares an if statement, two else ifs, and one else statement. See if statement section to see details on conditions|
|```p text]```| Prints text|
|```x=4```| Declares a variable. For equality and magnitude operators, see the operators section|
|```b={1 4 5 6```|This creates an array, which auto detects the type, integers in this case(autodetect only detects the first element, so if you want to make an array of doubles you dont need the decimal after each term), with length 4. 
|```aa b 1 3``` |"array adds" a three to the first spot of b|
|```ad b 1```|"array deletes" the first term of b. Can also put "L" or "f" to denote first or last|
|```ac b 1 4``` | "array changes" the first term of b to 4|
|```b=b*-1```|flips a backwards|
|```b=b*2```|duplicates each character in b|



