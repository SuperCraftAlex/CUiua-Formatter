# CUiua formatter
Code formatter for the [CUiua](https://github.com/SuperCraftAlex/CUiua) programming language.

## Building an executable
```sh
# first build the project
./gradlew nativeBinaries
# the executable is now located at build/bin/native/releaseExecutable/cuiua_fmt.kexe

# then you can copy the executable into your bin folder (linux)
sudo cp build/bin/native/releaseExecutable/cuiua_fmt.kexe /usr/bin/cuiua_fmt
```

## Usage
Just pipe the code into the executable and it will output the formatted code.

## Example
`castrealeach minred` -> `ℝ∵ ↧/`