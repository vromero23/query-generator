grammar DocumentType;

// Um DocumentType possui um nome um mapeamento ao ER-model
// e um conjunto de campos
docType:
    docTypeName=STRING erMapping '{' field* '}'
;

// Um mapeamento ao ER-model consiste de zero ou mais referências,
// separadas por vírgula ou não, a entidades ou relacionamentos
// do ER-model. As referências são feitas pelo nome.
//
// Para cada referência, indica-se se o mapeamento é principal (main=true)
// ou não (main=false).
//
// O mapeamento com (main=true) significa que:
//    - Todos os atributos da entidade ou relacionamento estão presentes no tipo de documento
//    - Todas as instâncias da entidade estarão armazenadas no DocumentType, sem repetição,
//         ou seja, uma consulta do tipo "findAll" neste DocumentType irá retornar todas
//         as instâncias possíveis da entidade, com todos os atributos possíveis
//
// O mapeamento com (main=false) significa que pelo menos uma
//    das seguintes situações é verdadeira:
//    - O documento não tem todos os atributos da entidade mapeada
//    - Uma consulta do tipo "findAll" neste DocumentType retorna instâncias duplicadas
//    - Uma consulta do tipo "findAll" neste DocumentType não retorna todas as
//         instâncias possíveis da entidade
//
// Se uma entidade está mapeada, o document type deve possuir pelo menos
// um campo mapeado a um atributo desta entidade
erMapping:
    '['
    (
        erName=STRING '(' ['main=true'|'main=false'] ')' ','?
    )*
    ']'
;

// Um mapeamento de um campo consiste de um nome de entidade/relacionamento
// e um nome do atributo.
//
// O atributo deve pertencer à entidade/relacionamento
//
// A entidade/relacionamento deve ter sido declarada na regra "erMapping"
// dentro da qual este "fieldMapping" se encontra
fieldMapping:
    '[' (erName=STRING '.' attributeName=STRING)? ']'
;

// Um campo de um DocumentType pode ser simples ou embutir um outro DocumentType
field:
    simpleField | embeddedDocumentField
;

// Um campo simples tem nome e tipo
simpleField:
    fieldName=STRING ':' fieldType=STRING fieldMapping
;

// Um campo que embute outro DocumentType tem um nome e o outro DocumentType
embeddedDocumentField:
    fieldName=STRING ':' docType
;
 
/*
Exemplo 1:
- correto

DocTypeMatricula [ Aluno(main=true) , Disciplina(main=false) ]
{
    nome            : string [ Aluno.nome ]
    endereco        : string [ Aluno.endereco ]
    cod_disciplina  : int [ Disciplina.codigo ]
    curso           : SubDocTypeCurso [ Curso(main=false) ]
    {
        cod_curso   : int [ Curso.codigo ]
        nome_curso  : string [ Curso.nome ]
    }
}

Exemplo 2:
- incorreto, pois Disciplina não aparece mapeada em
  DocTypeMatricula, mas cod_disciplina referencia a
  entidade Disciplina

DocTypeMatricula [ Aluno(main=true) ]
{
    nome            : string [ Aluno.nome ]
    endereco        : string [ Aluno.endereco ]
    cod_disciplina  : int [ Disciplina.codigo ]
    curso           : SubDocTypeCurso [ Curso(main=false) ]
    {
        cod_curso   : int [ Curso.codigo ]
        nome_curso  : string [ Curso.nome ]
    }
}

Exemplo 3:
- incorreto, pois Disciplina aparece mapeada em
  DocTypeMatricula, mas não há nenhum atributo
  dessa entidade em DocTypeMatricula

DocTypeMatricula [ Aluno(main=true) , Disciplina(main=false) ]
{
    nome            : string [ Aluno.nome ]
    endereco        : string [ Aluno.endereco ]
    curso           : SubDocTypeCurso [ Curso(main=false) ]
    {
        cod_curso   : int [ Curso.codigo ]
        nome_curso  : string [ Curso.nome ]
    }
}

Exemplo 4:
- incorreto, pois Disciplina aparece mapeada em
  DocTypeMatricula, mas não há nenhum atributo
  dessa entidade em DocTypeMatricula (existe
  o atributo cod_disciplina, mas ele não pertence
  diretamente a DocTypeMatricula)

DocTypeMatricula [ Aluno(main=true) , Disciplina(main=false) ]
{
    nome            : string [ Aluno.nome ]
    endereco        : string [ Aluno.endereco ]
    disciplina      : SubDocTypeDisciplina [ Disciplina(main=false) ]
    {
        cod_disciplina  : string [ Disciplina.codigo ]
        nome_disciplina : string [ Disciplina.nome ]
    }
    curso           : SubDocTypeCurso [ Curso(main=false) ]
    {
        cod_curso   : int [ Curso.codigo ]
        nome_curso  : string [ Curso.nome ]
    }
}

Exemplo 5:
- incorreto, pois nome_disciplina referencia a
  entidade Disciplina, que não está mapeada
  no DocumentType que o contém. Obs: Disciplina
  está mapeada em DocTypeMatricula, mas o mapeamento
  deve estar declarado no DocumentType que contém
  imediatamente os atributos

DocTypeMatricula [ Aluno(main=true) , Disciplina(main=false) ]
{
    nome                : string [ Aluno.nome ]
    endereco            : string [ Aluno.endereco ]
    cod_disciplina      : string [ Disciplina.codigo ]
    detalhes_disciplina : SubDocTypeDisciplina []
    {
        nome_disciplina : string [ Disciplina.nome ]
    }
    curso               : SubDocTypeCurso [ Curso(main=false) ]
    {
        cod_curso       : int [ Curso.codigo ]
        nome_curso      : string [ Curso.nome ]
    }
}
*/