# RodaFácil Locadora

Sistema de locadora de carros em Java, executado pelo console. Trabalho da P2 de Programação Orientada a Objetos (ICEV).

O sistema controla frota, clientes, funcionários e todo o ciclo de um aluguel: reserva, retirada, devolução, cobrança e fechamento. Os dados ficam salvos em arquivo JSON.

## Integrantes

| Integrante | Parte |
|---|---|
| Luiz Henrique | Carros |
| Hilterlan Silva da Costa | Clientes |
| Gustavo Lima | Aluguéis |
| Emanuel Sousa | Persistência |
| Nivaldo Cardoso | Telas |

## Requisitos

- **JDK 11 ou superior** (o código usa recursos do Java 11, como `Files.writeString` e `String.isBlank`).
- Nenhuma biblioteca externa. Não precisa de Maven, Gradle nem de baixar JARs.

Para conferir a versão instalada:

```bash
java -version
javac -version
```

## Como executar

### Opção 1: pelo terminal

Na raiz do projeto (a pasta que contém `src/`):

**Linux / macOS**

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out Telas.RodaFacil
```

**Windows (PowerShell)**

```powershell
chcp 65001
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Telas.RodaFacil
```

> No Windows, o comando `chcp 65001` evita que os acentos apareçam errados no console.

### Opção 2: pelo IntelliJ IDEA

1. Abra a pasta do projeto (`File > Open`).
2. Confirme que o JDK do projeto é 11 ou superior (`File > Project Structure`).
3. Abra a classe `src/Telas/RodaFacil.java` e clique em **Run**.

## Como usar

Ao iniciar, aparece o menu principal:

```
1 - Carros
2 - Clientes
3 - Funcionários
4 - Aluguéis
5 - Painel gerencial
6 - Carregar dados de exemplo
0 - Sair
```

Na primeira execução, use a opção **6** para carregar carros, clientes e funcionários de demonstração. Em todos os menus, a opção `0` volta ou sai. As entradas inválidas são recusadas e o sistema pergunta de novo.

Fluxo típico de um aluguel: cadastrar cliente e carro, reservar, retirar (com o funcionário que fez a vistoria), devolver e ver o fechamento com multas e extras.

## Onde ficam os arquivos

Os arquivos são criados na pasta **de onde o programa foi executado**:

| Caminho | Conteúdo |
|---|---|
| `dados/locadora.json` | Banco com carros, clientes, funcionários e aluguéis (salvo após cada tela e ao sair) |
| `dados/historico.txt` | Histórico de ações (data, funcionário, ação e detalhes) |
| `dados/pdf/` | Contratos e recibos em PDF gerados pela tela de aluguéis |

Se `dados/locadora.json` não existir, um banco vazio é criado. Se o arquivo estiver corrompido, o original é preservado como `locadora.corrompido-<data>.json` e um banco novo é iniciado.

## Backup no Google Drive (opcional)

A classe `BackupDrive` copia a pasta `dados` para uma pasta sincronizada pelo **Google Drive para computador**. Para configurar, defina a variável de ambiente com o caminho dessa pasta:

```bash
# Linux / macOS
export RODAFACIL_DRIVE_DIR="/caminho/para/Google Drive/RodaFacil"

# Windows (PowerShell)
$env:RODAFACIL_DRIVE_DIR = "G:\Meu Drive\RodaFacil"
```

Sem a variável, o sistema funciona normalmente, sem backup. Não é usada a API do Google, então não há chaves nem credenciais (não é necessário `.env`).

## Testes

Cada pacote tem uma classe de teste executável, que imprime `[OK]` ou `[FALHA]` para cada verificação. Depois de compilar:

```bash
java -cp out Clientes.TesteSemana4
java -cp out Carros.TesteSemana4
java -cp out Alugueis.TesteSemana4
java -cp out Persistencia.TesteParte4
```

## Estrutura do projeto

```
src/
├── Carros/        Veiculo (abstrata), Passeio, SUV, Utilitario, Eletrico, Manutencao, Locadora
├── Clientes/      Pessoa, Cliente, Funcionario, CNH, NivelFidelidade, CadastroClientes, CadastroFuncionarios
├── Alugueis/      Locacao, GerenciadorLocacoes, Orcamento, Fechamento, Seguro, FormaPagamento, exceções
├── Persistencia/  BancoDados, JsonSimples, ConversorObjetos, ExportadorCsv, HistoricoAcoes, BackupDrive
├── Relatorios/    PdfSimples, DocumentosLocacao
└── Telas/         Tela (abstrata), MenuPrincipal, TelaCarros, TelaClientes, TelaAlugueis, PainelGerencial, RodaFacil (main)
```

## Princípios de POO aplicados

- **Herança:** `Pessoa` -> `Cliente`/`Funcionario`; `Veiculo` -> `Passeio`/`SUV`/`Utilitario`/`Eletrico`; `LocadoraException` -> exceções específicas.
- **Abstração e polimorfismo:** `Veiculo`, `Seguro`, `FormaPagamento` e `Tela` são abstratas; cada subclasse calcula valores ou se comporta do seu jeito.
- **Encapsulamento:** atributos privados, alterados só por setters que validam o valor (CPF, idade, placa, CNH).
- **Sobrecarga:** `reservar` e `devolver` têm várias versões.

## Regras de negócio principais

- **Habilitação:** SUV e elétrico exigem 21 anos ou mais e CNH há pelo menos 2 anos. CNH vencida ou categoria A (moto) não aluga carro.
- **Fidelidade:** 1 ponto a cada R$ 10,00 pagos. Prata (500 pontos, 5%), Ouro (1.500, 10%) e Diamante (3.000, 15%).
- **Bloqueio:** o cliente com multa ou avaria em aberto não aluga até quitar o débito.
- **Revisão:** o carro com 10.000 km desde a última revisão fica indisponível.
- **Pagamento:** Pix com 5% de desconto; cartão em até 6x (acréscimo de 5% de 4x a 6x, parcela mínima de R$ 100,00).
